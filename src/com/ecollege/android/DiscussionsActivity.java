package com.ecollege.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.HeaderAdapter;
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
	
	public static final String USER_TOPIC_EXTRA = "USER_TOPIC_EXTRA";

	private static final int VIEW_TOPIC_REQUEST = 0;
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.string.last_updated_s) String lastUpdatedFormat;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectView(R.id.course_dropdown) Spinner courseDropdown;
	@InjectView(android.R.id.empty) View noResultsView;
	
	protected ECollegeClient client;
	private long topicsLastUpdated;
	private TopicsHeaderAdapter topicHeaderAdapter;
	private TopicsAdapter topicAdapter;
	private LayoutInflater viewInflater;
	private ArrayList<String> courseDropdownTitles;
	private HashMap<String, Course> courseTitleToCourseMap;
	private long selectedCourseId;
	protected TextView lastUpdatedText;
	private View lastUpdatedHeader;

	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussions);
        client = app.getClient();
        viewInflater = getLayoutInflater();
        
        loadCourseTitles();
        configureControls();
        buildLastUpdatedHeader();
        loadAndDisplayTopicsForSelectedCourses();
    }
    
    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == VIEW_TOPIC_REQUEST) {
    		reloadAndDisplayTopicsForSelectedCourses();
    	}
    }
    
	protected void buildLastUpdatedHeader() {
		lastUpdatedHeader = getLayoutInflater().inflate(R.layout.last_updated_view, null);
		lastUpdatedText = (TextView) lastUpdatedHeader.findViewById(R.id.last_updated_text);
    	getListView().addHeaderView(lastUpdatedHeader, null, false);
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
				reloadAndDisplayTopicsForSelectedCourses();
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
		if (position == 0) {
			selectedCourseId = 0;
		} else {
			String title = courseDropdownTitles.get(position);
			selectedCourseId = courseTitleToCourseMap.get(title).getId();
		}
		topicAdapter = null;
		topicHeaderAdapter = null;
		loadAndDisplayTopicsForSelectedCourses();
	}

	private void loadAndDisplayTopicsForSelectedCourses() {
		String formattedLastUpdated = getString(R.string.never);
		if (topicsLastUpdated != 0) {
			formattedLastUpdated = new Date(topicsLastUpdated).toString();
		}
		lastUpdatedText.setText(String.format(lastUpdatedFormat, formattedLastUpdated));
		noResultsView.setVisibility(View.INVISIBLE);
		setListAdapter(createOrReturnTopicAdapter(false));
	}
	
	private void reloadAndDisplayTopicsForSelectedCourses() {
		topicAdapter = new TopicsAdapter(this, new ArrayList<UserDiscussionTopic>());
		topicHeaderAdapter = new TopicsHeaderAdapter(this, topicAdapter);
		noResultsView.setVisibility(View.INVISIBLE);
		fetchTopicsForSelectedCourses(true);
	}

	private ListAdapter createOrReturnTopicAdapter(boolean reload) {
		if (topicAdapter == null && topicHeaderAdapter == null) {
			topicAdapter = new TopicsAdapter(this, new ArrayList<UserDiscussionTopic>());
			topicHeaderAdapter = new TopicsHeaderAdapter(this, topicAdapter);
			fetchTopicsForSelectedCourses(reload);
		}
		return topicHeaderAdapter;
	}
	
	private void fetchTopicsForSelectedCourses(boolean reload) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		ArrayList<String> courseIds = getSelectedCourseId();
		buildService(new FetchDiscussionTopicsForCourseIds(courseIds))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallSuccess(FetchDiscussionTopicsForCourseIds service) {
		noResultsView.setVisibility(View.VISIBLE);
		topicAdapter.setNotifyOnChange(false);
		for (UserDiscussionTopic topic : service.getResult()) {
			topicAdapter.add(topic);
		}
		topicAdapter.setNotifyOnChange(true);
		topicAdapter.notifyDataSetChanged();
		topicsLastUpdated = service.getCompletedAt();
		loadAndDisplayTopicsForSelectedCourses();
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
		UserDiscussionTopic selectedTopic = (UserDiscussionTopic)getListAdapter().getItem(position);
		Intent intent = new Intent(this, UserTopicActivity.class);
		intent.putExtra(USER_TOPIC_EXTRA, selectedTopic);
		startActivityForResult(intent, VIEW_TOPIC_REQUEST);
	}
    
	static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
    }
    
    protected class TopicsHeaderAdapter extends HeaderAdapter {

		public TopicsHeaderAdapter(Context context, ListAdapter baseAdapter) {
			super(context, baseAdapter);
			setListHeaderCount(getListView().getHeaderViewsCount());
		}
		
		@Override public String headerLabelFunction(Object item, int position) {
			UserDiscussionTopic userTopic = (UserDiscussionTopic)item;
			DiscussionTopic topic = userTopic.getTopic();
			ContainerInfo info = topic.getContainerInfo();
			return Html.fromHtml(info.getCourseTitle()).toString();
		}
    }
    
	protected class TopicsAdapter extends ArrayAdapter<UserDiscussionTopic> {

		public TopicsAdapter(Context context, List<UserDiscussionTopic> topicList) {
			super(context, R.layout.user_topic_item, topicList);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
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
			
			UserDiscussionTopic userTopic = getItem(position);
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