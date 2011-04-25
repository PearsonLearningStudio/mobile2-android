package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.TopicsAdapter;
import com.ecollege.android.adapter.TopicsAdapter.TopicAdapterMode;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicsForThreadId;
import com.google.inject.Inject;

public class CourseThreadActivity extends ECollegeListActivity {
	
	public static final String THREAD_ID_EXTRA = "THREAD_ID_EXTRA";
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	
	@InjectExtra(COURSE_EXTRA) Course course;
	@InjectExtra(THREAD_ID_EXTRA) long threadId;
	
	private TopicsAdapter topicAdapter;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		titleText.setText(R.string.thread_topics);
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
		
		topicAdapter = new TopicsAdapter(this,TopicAdapterMode.NO_GROUP_NO_FILTER);
		setListAdapter(topicAdapter);
		updateCurrentTopics(false);
	}
	
    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == UserTopicActivity.VIEW_TOPIC_REQUEST) {
    		updateCurrentTopics(true);
    	}
    }
    
	private void updateCurrentTopics(boolean reload) {
		topicAdapter.beginLoading();
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		buildService(new FetchDiscussionTopicsForThreadId(course.getId(), threadId))
			.configureCaching(cacheConfiguration)
			.execute();
	}

    public void onServiceCallSuccess(FetchDiscussionTopicsForThreadId service) {
    	topicAdapter.updateItems(service.getResult());
    }
    public void onServiceCallException(FetchDiscussionTopicsForThreadId service, Exception ex) {
    	topicAdapter.hasError();
    }
    
	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		UberItem<UserDiscussionTopic> item = (UberItem<UserDiscussionTopic>)l.getItemAtPosition(position);
		
		UserDiscussionTopic selectedTopic = item.getDataItem();
		Intent intent = new Intent(this, UserTopicActivity.class);
		intent.putExtra(UserTopicActivity.USER_TOPIC_EXTRA, selectedTopic);
		startActivityForResult(intent, UserTopicActivity.VIEW_TOPIC_REQUEST);
		
	}
}
