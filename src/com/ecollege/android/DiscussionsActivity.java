package com.ecollege.android;

import java.util.ArrayList;
import java.util.HashMap;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.adapter.UberItem.UberItemType;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ContainerInfo;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicsForCourseIds;
import com.google.inject.Inject;

public class DiscussionsActivity extends ECollegeListActivity {
	

	private static final int VIEW_TOPIC_REQUEST = 0;
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectView(R.id.course_dropdown) Spinner courseDropdown;
	
	protected ECollegeClient client;
	private TopicsAdapter topicAdapter;
	private LayoutInflater viewInflater;
	private ArrayList<String> courseDropdownTitles;
	private HashMap<String, Course> courseTitleToCourseMap;
	private long selectedCourseId;
	private boolean firstLoadFinished = false;
	
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussions);
        client = app.getClient();
        viewInflater = getLayoutInflater();
     
        loadCourseTitles();
        configureControls();

        topicAdapter = new TopicsAdapter(this);
        setListAdapter(topicAdapter);
		updateCurrentTopics(false);
    }
    
    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == VIEW_TOPIC_REQUEST) {
    		updateCurrentTopics(true);
    	}
    }
    
	private void loadCourseTitles() {
        courseDropdownTitles = new ArrayList<String>();
        courseTitleToCourseMap = new HashMap<String, Course>();
        courseDropdownTitles.add(getString(R.string.all_courses));
        String courseTitle;
        for (Course course : app.getCurrentCourseList()) {
        	courseTitle = Html.fromHtml(course.getTitle()).toString();
        	courseDropdownTitles.add(courseTitle);
        	courseTitleToCourseMap.put(courseTitle, course);
        }
	}

	private void configureControls() {
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateCurrentTopics(true);
			}
		});
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, R.layout.transparent_spinner_text_view, courseDropdownTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseDropdown.setAdapter(adapter);
		courseDropdown.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				courseSelected(arg2);
			}

			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}

	protected void courseSelected(int position) {
		if (firstLoadFinished) {
			if (position == 0) {
				selectedCourseId = 0;
			} else {
				String title = courseDropdownTitles.get(position);
				selectedCourseId = courseTitleToCourseMap.get(title).getId();
			}
			updateCurrentTopics(false);
		}
	}

	private void updateCurrentTopics(boolean reload) {
		topicAdapter.beginLoading();
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		ArrayList<String> courseIds = getSelectedCourseId();
		buildService(new FetchDiscussionTopicsForCourseIds(courseIds))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallException(FetchDiscussionTopicsForCourseIds service, Exception ex) {
		firstLoadFinished = true;
		topicAdapter.hasError();
	}
	
	public void onServiceCallSuccess(FetchDiscussionTopicsForCourseIds service) {
		firstLoadFinished = true;
		topicAdapter.setLastUpdatedAt(service.getCompletedAt());
		topicAdapter.updateItems(service.getResult());
	}

	private ArrayList<String> getSelectedCourseId() {
		boolean allCoursesSelected = (selectedCourseId == 0);
		ArrayList<String> ids = new ArrayList<String>();
		if (allCoursesSelected) { // all courses selected
			for (Course course : app.getCurrentCourseList()) {
				ids.add(Long.toString(course.getId()));
			}
		} else {
			ids.add(Long.toString(selectedCourseId));
		}
		return ids;
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		@SuppressWarnings("unchecked")
		UberItem<UserDiscussionTopic> item = (UberItem<UserDiscussionTopic>)l.getItemAtPosition(position);
		
		if (item.getItemType() == UberItemType.FOOTER) {
			CourseGroupId groupId = (CourseGroupId) item.getGroupId();
			long courseId = groupId.getCourseId();

			Intent intent = new Intent(this, CourseDiscussionsActivity.class);
			intent.putExtra(CourseDiscussionsActivity.COURSE_ID_EXTRA, courseId);
			startActivity(intent);
		} else {
			UserDiscussionTopic selectedTopic = item.getDataItem();
			Intent intent = new Intent(this, UserTopicActivity.class);
			intent.putExtra(UserTopicActivity.USER_TOPIC_EXTRA, selectedTopic);
			startActivityForResult(intent, VIEW_TOPIC_REQUEST);
		}
	}
    
	static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
    }
    
	static class FooterViewHolder {
		TextView linkText;
	}
	
	
	private class CourseGroupId {
		
		private long courseId;
		private String courseTitle;
		
		public CourseGroupId(long courseId, String courseTitle) {
			this.courseId = courseId;
			this.courseTitle = courseTitle;
		}
		
		public long getCourseId() {
			return courseId;
		}
		
		@Override
		public String toString() {
			return courseTitle;
		}
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof CourseGroupId)) return false;
			return ((CourseGroupId)o).getCourseId() == getCourseId();
		}
	}
    
	protected class TopicsAdapter extends UberAdapter<UserDiscussionTopic> {

		public TopicsAdapter(Context context) {
			super(context,true,true,false);
		}
		
		@Override
		public boolean isEnabled(int position) {
			UberItem<UserDiscussionTopic> item = getItem(position);
			
			if (item.getItemType() == UberItemType.FOOTER) {
				return true;
			} else {
				return super.isEnabled(position);
			}
		}
		
		@Override
		protected Object groupIdFunction(UserDiscussionTopic item) {
			UserDiscussionTopic userTopic = (UserDiscussionTopic)item;
			DiscussionTopic topic = userTopic.getTopic();
			ContainerInfo info = topic.getContainerInfo();
			
			return new CourseGroupId(info.getCourseId(), Html.fromHtml(info.getCourseTitle()).toString());
		}
		
		@Override
		protected View getFooterView(int position, View convertView,
				ViewGroup parent, Object groupId) {
			FooterViewHolder holder;

	        if (convertView == null) {
	            convertView = ((Activity)parent.getContext()).getLayoutInflater().inflate(R.layout.see_all_discussions, null);

	            holder = new FooterViewHolder();
	            holder.linkText = (TextView) convertView.findViewById(R.id.see_all_text);
	            convertView.setTag(holder);
	        } else {
	            holder = (FooterViewHolder) convertView.getTag();
	        }
	        
	        holder.linkText.setText("See all topics for " + groupId.toString());
	        return convertView;
		}
		
		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<UserDiscussionTopic> item) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = viewInflater.inflate(R.layout.user_topic_item, null);
				
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
				holder.totalResponseCountText = (TextView) convertView.findViewById(R.id.total_response_count_text);
				holder.unreadResponseCountText = (TextView) convertView.findViewById(R.id.unread_response_count_text);
				holder.userResponseCountText = (TextView) convertView.findViewById(R.id.user_response_count_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			UserDiscussionTopic userTopic = item.getDataItem();
			DiscussionTopic topic = userTopic.getTopic();
			ResponseCount responseCount = userTopic.getChildResponseCounts();
			holder.titleText.setText(Html.fromHtml(topic.getTitle()).toString());
			
			ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(
					getBaseContext(),
					holder.icon,
					holder.unreadResponseCountText,
					holder.totalResponseCountText,
					holder.userResponseCountText
			);
			responseCountViewHelper.setResponseCount(responseCount);
			
			return convertView;
		}

		
	}
}