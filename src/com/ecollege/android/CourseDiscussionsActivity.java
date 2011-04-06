package com.ecollege.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import roboguice.inject.InjectExtra;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ContainerInfo;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicsForCourseIds;
import com.google.inject.Inject;

public class CourseDiscussionsActivity extends ECollegeListActivity {
	
	public static final String COURSE_ID_EXTRA = "COURSE_ID_EXTRA";
	private static final int VIEW_TOPIC_REQUEST = 0;
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectView(R.id.unit_dropdown) Spinner unitDropdown;
	@InjectExtra(COURSE_ID_EXTRA) long courseId;
	
	protected ECollegeClient client;
	private TopicsAdapter topicAdapter;
	private LayoutInflater viewInflater;
	private List<UserDiscussionTopic> allTopics = new ArrayList<UserDiscussionTopic>();
	private List<CourseUnit> allUnits = new ArrayList<CourseDiscussionsActivity.CourseUnit>();
	
	private CourseUnit currentUnitFilter = new CourseUnit();
	
	private class CourseUnit implements Comparable<CourseUnit> {
		public CourseUnit() {
		}
		public CourseUnit(long unitNumber, String unitHeader, String unitTitle) {
			this.unitNumber = unitNumber;
			this.unitHeader = unitHeader;
			this.unitTitle = unitTitle;
		}
		public long unitNumber = -1;
		public String unitHeader;
		public String unitTitle;
		
		public long getUnitNumber() {
			return unitNumber;
		}
		
		public int compareTo(CourseUnit another) {
			if (unitNumber == -1) return -1; //All at top
			if (another == null) return 1;
			return toString().compareTo(another.toString());
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof CourseUnit)) return false;
			return ((CourseUnit)o).toString().equals(toString());
		}
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		@Override
		public String toString() {
			if (unitNumber == -1) {
				return "All Units";
			}
            return String.format("%s %s: %s", unitHeader, unitNumber, unitTitle);
		}
	}
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_discussions);
        client = app.getClient();
        viewInflater = getLayoutInflater();
        
        configureControls();

		topicAdapter = new TopicsAdapter(this);
        setListAdapter(topicAdapter);
        fetchTopicsForSelectedCourses(false);
    }
    
    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == VIEW_TOPIC_REQUEST) {
			fetchTopicsForSelectedCourses(true);
    	}
    }

	private void configureControls() {
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fetchTopicsForSelectedCourses(true);
			}
		});
        
        unitDropdown.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				Object item = parent.getItemAtPosition(position);
				
				if (item instanceof CourseUnit) {
					currentUnitFilter = (CourseUnit)item;
					updateCurrentTopics();
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				currentUnitFilter = new CourseUnit();//All Item
				updateCurrentTopics();
			}
        	
		});
        
	}
	
	private void fetchTopicsForSelectedCourses(boolean reload) {
		topicAdapter.beginLoading();
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		buildService(new FetchDiscussionTopicsForCourseIds(courseId))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallException(FetchDiscussionTopicsForCourseIds service, Exception ex) {
		topicAdapter.hasError();
	}
	
	public void onServiceCallSuccess(FetchDiscussionTopicsForCourseIds service) {
		allTopics = service.getResult();
		
		HashSet<CourseUnit> unitSet = new HashSet<CourseDiscussionsActivity.CourseUnit>();
		
		unitSet.add(new CourseUnit());
		
		for (UserDiscussionTopic t : allTopics) {
			ContainerInfo ci = t.getTopic().getContainerInfo();
			unitSet.add(new CourseUnit(ci.getUnitNumber(),ci.getUnitHeader(),ci.getUnitTitle()));
		}
		
		List<CourseUnit> sortedUnits = new ArrayList<CourseDiscussionsActivity.CourseUnit>(unitSet);
		Collections.sort(sortedUnits);
		
		allUnits = sortedUnits;
		
		ArrayAdapter<CourseUnit> adapter = new ArrayAdapter<CourseUnit> (this, R.layout.transparent_spinner_text_view, allUnits);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitDropdown.setAdapter(adapter);
		
		topicAdapter.setLastUpdatedAt(service.getCompletedAt());
		updateCurrentTopics();
	}
	
	protected void updateCurrentTopics() {
		if (currentUnitFilter == null || currentUnitFilter.getUnitNumber() == -1) {
			topicAdapter.updateItems(allTopics);
		} else {
			List<UserDiscussionTopic> filteredTopics = new ArrayList<UserDiscussionTopic>();
			
			for (UserDiscussionTopic userTopic : allTopics) {
				ContainerInfo ci = userTopic.getTopic().getContainerInfo();
				if (ci.getUnitNumber() == currentUnitFilter.getUnitNumber()) {
					filteredTopics.add(userTopic);
				}
			}
			topicAdapter.updateItems(filteredTopics);
		}
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		@SuppressWarnings("unchecked")
		UberItem<UserDiscussionTopic> item = (UberItem<UserDiscussionTopic>)l.getItemAtPosition(position);
		
		UserDiscussionTopic selectedTopic = item.getDataItem();
		Intent intent = new Intent(this, UserTopicActivity.class);
		intent.putExtra(UserTopicActivity.USER_TOPIC_EXTRA, selectedTopic);
		startActivityForResult(intent, VIEW_TOPIC_REQUEST);
	}
    
	static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
    }
	
	protected class TopicsAdapter extends UberAdapter<UserDiscussionTopic> {
  
		
		public TopicsAdapter(Context context) {
			super(context, true, false, false);
		}

		@Override
		protected Object groupIdFunction(UserDiscussionTopic item) {
            ContainerInfo ci = item.getTopic().getContainerInfo();
            return String.format("%s %s: %s", ci.getUnitHeader(), ci.getUnitNumber(), ci.getUnitTitle());
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