package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.UserDiscussionTopic;

public class UserTopicActivity extends ECollegeListActivity {
	
	@InjectExtra(DiscussionsActivity.USER_TOPIC_EXTRA) protected UserDiscussionTopic userTopic;
	@InjectView(R.id.topic_title_text) protected TextView topicTitleText;
	
	protected DiscussionTopic topic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_topic);
		topic = userTopic.getTopic();
		topicTitleText.setText(topic.getTitle());
	}

}
