package com.ecollege.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.HeaderAdapter;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ContainerInfo;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicsForCourseIds;
import com.google.inject.Inject;

public class DiscussionsActivity extends ECollegeListActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.last_updated_text) TextView lastUpdatedText;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectResource(R.string.d_total_reponses) String totalResponsesFormat;
	@InjectResource(R.string.d_total_reponse) String totalResponseFormat;
	@InjectResource(R.string.d_responses_by_you) String responsesByYouFormat;
	@InjectResource(R.string.d_response_by_you) String responseByYouFormat;
	@InjectResource(R.string.no_responses) String noResponsesString;
	
	protected ECollegeClient client;
	private long topicsLastUpdated;
	private TopicsHeaderAdapter topicHeaderAdapter;
	private TopicsAdapter topicAdapter;
	private LayoutInflater viewInflater;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussions);
        client = app.getClient();
        viewInflater = getLayoutInflater();
        
        loadAndDisplayTopicsForSelectedCourses();
        
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadAndDisplayTopicsForSelectedCourses();
			}
		});
    }

	private void loadAndDisplayTopicsForSelectedCourses() {
		String formattedLastUpdated = getString(R.string.never);
		if (topicsLastUpdated != 0) {
			formattedLastUpdated = new Date(topicsLastUpdated).toString();
		}
		lastUpdatedText.setText(formattedLastUpdated);
		setListAdapter(createOrReturnTopicAdapter(false));
	}
	
	private void reloadAndDisplayTopicsForSelectedCourses() {
		topicAdapter = new TopicsAdapter(this, new ArrayList<UserDiscussionTopic>());
		topicHeaderAdapter = new TopicsHeaderAdapter(this, topicAdapter);
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
		ArrayList<String> courseIds = getSelectedCourseIds();
		buildService(new FetchDiscussionTopicsForCourseIds(courseIds))
		.configureCaching(cacheConfiguration)
		.execute();
	}
	
	public void onServiceCallSuccess(FetchDiscussionTopicsForCourseIds service) {
		topicAdapter.setNotifyOnChange(false);
		for (UserDiscussionTopic topic : service.getResult()) {
			topicAdapter.add(topic);
		}
		topicAdapter.setNotifyOnChange(true);
		topicAdapter.notifyDataSetChanged();
		topicsLastUpdated = service.getCompletedAt();
		loadAndDisplayTopicsForSelectedCourses();
	}

	private ArrayList<String> getSelectedCourseIds() {
		boolean allCoursesSelected = true;
		if (allCoursesSelected) { // all courses selected
			ArrayList<String> ids = new ArrayList<String>();
			for (Course course : app.getCurrentCourseList()) {
				ids.add(Long.toString(course.getId()));
			}
			return ids;
		} else {
			return null;
			// figure out what's selected
		}
	}
	
    static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView timeText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
    }
    
    protected class TopicsHeaderAdapter extends HeaderAdapter {

		public TopicsHeaderAdapter(Context context, ListAdapter baseAdapter) {
			super(context, baseAdapter);
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
			// TODO Auto-generated constructor stub
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = viewInflater.inflate(R.layout.user_topic_item, null);
				
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
				holder.timeText = (TextView) convertView.findViewById(R.id.time_text);
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
			String correctFormat = "%d";
			
			holder.titleText.setText(Html.fromHtml(topic.getTitle()).toString());
			if (responseCount.getUnreadResponseCount() == 0) {
				holder.unreadResponseCountText.setVisibility(View.GONE);
			} else {
				holder.unreadResponseCountText.setText(Long.toString(responseCount.getUnreadResponseCount()));
				holder.unreadResponseCountText.setVisibility(View.VISIBLE);
			}
			
			if (responseCount.getTotalResponseCount() == 0) {
				holder.totalResponseCountText.setText(noResponsesString);
			} else {
				correctFormat = (responseCount.getTotalResponseCount() == 1) ? totalResponseFormat : totalResponsesFormat; 
				holder.totalResponseCountText.setText(String.format(correctFormat, responseCount.getTotalResponseCount()));
			}
			
			if (responseCount.getPersonalResponseCount() == 0) {
				holder.userResponseCountText.setVisibility(View.GONE);
			} else {
				correctFormat = (responseCount.getPersonalResponseCount() == 1) ? responseByYouFormat : responsesByYouFormat; 
				holder.userResponseCountText.setText(String.format(correctFormat, responseCount.getPersonalResponseCount()));
				holder.userResponseCountText.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		
	}
}