package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionTopic;

public class UserTopicActivity extends ECollegeListActivity {
	
	@InjectExtra(DiscussionsActivity.USER_TOPIC_EXTRA) protected UserDiscussionTopic userTopic;
	@InjectView(R.id.topic_title_text) protected TextView topicTitleText;
    @InjectView(R.id.icon) protected ImageView topicIcon;
    @InjectView(R.id.title_text) protected TextView userTopicTitleText;
    @InjectView(R.id.total_response_count_text) protected TextView totalResponseCountText;
    @InjectView(R.id.unread_response_count_text) protected TextView unreadResponseCountText;
    @InjectView(R.id.user_response_count_text) protected TextView userResponseCountText;
	@InjectResource(R.string.d_total_reponses) String totalResponsesFormat;
	@InjectResource(R.string.d_total_reponse) String totalResponseFormat;
	@InjectResource(R.string.d_responses_by_you) String responsesByYouFormat;
	@InjectResource(R.string.d_response_by_you) String responseByYouFormat;
	@InjectResource(R.string.no_responses) String noResponsesString;
	
	protected DiscussionTopic topic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_topic);
		topic = userTopic.getTopic();
		ResponseCount responseCount = userTopic.getChildResponseCounts();
		String htmlSafeTitle = Html.fromHtml(topic.getTitle()).toString();
		topicTitleText.setText(htmlSafeTitle);
		userTopicTitleText.setText(htmlSafeTitle);
		
		String correctFormat = "%d";
		if (responseCount.getUnreadResponseCount() == 0) {
			unreadResponseCountText.setVisibility(View.GONE);
		} else {
			unreadResponseCountText.setText(Long.toString(responseCount.getUnreadResponseCount()));
			unreadResponseCountText.setVisibility(View.VISIBLE);
		}
		
		if (responseCount.getTotalResponseCount() == 0) {
			totalResponseCountText.setText(noResponsesString);
		} else {
			correctFormat = (responseCount.getTotalResponseCount() == 1) ? totalResponseFormat : totalResponsesFormat; 
			totalResponseCountText.setText(String.format(correctFormat, responseCount.getTotalResponseCount()));
		}
		
		if (responseCount.getPersonalResponseCount() == 0) {
			userResponseCountText.setVisibility(View.GONE);
		} else {
			correctFormat = (responseCount.getPersonalResponseCount() == 1) ? responseByYouFormat : responsesByYouFormat; 
			userResponseCountText.setText(String.format(correctFormat, responseCount.getPersonalResponseCount()));
			userResponseCountText.setVisibility(View.VISIBLE);
		}
		
	}

}
