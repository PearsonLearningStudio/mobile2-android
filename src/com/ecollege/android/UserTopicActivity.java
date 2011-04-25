package com.ecollege.android;

import roboguice.inject.InjectExtra;
import android.os.Bundle;
import android.text.Html;

import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicById;

public class UserTopicActivity extends UserDiscussionActivity {
	
	public static final int VIEW_TOPIC_REQUEST = 0;
	public static final String TOPIC_ID_EXTRA = "TOPIC_ID_EXTRA";
	public static final String USER_TOPIC_EXTRA = "USER_TOPIC_EXTRA";
	
	@InjectExtra(value=USER_TOPIC_EXTRA,optional=true) protected UserDiscussionTopic userTopic;
	@InjectExtra(value=TOPIC_ID_EXTRA,optional=true) protected Long topicId;

	@Override
	protected UserDiscussionTopic getUserTopic() {
		return userTopic;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussion);
		
		setupView();
		
		if (userTopic == null) {
			lastSavedInstanceState = savedInstanceState;
			buildService(new FetchDiscussionTopicById(getApp().getCurrentUser().getId(), topicId)).execute();
		} else {
			populateView(savedInstanceState);
		}
	}

	public void onServiceCallSuccess(FetchDiscussionTopicById service) {
		userTopic = service.getResult();
		populateView(lastSavedInstanceState);
	}
	
	protected void populateView(Bundle savedInstanceState) {
		DiscussionTopic topic = userTopic.getTopic();
		responseCount = userTopic.getChildResponseCounts();
		styledDescriptionHtml = Html.fromHtml(topic.getDescription());

		headerViewHolder.parentTitleText.setText(Html.fromHtml(topic.getTitle()));

		String htmlSafeTitle = Html.fromHtml(topic.getTitle()).toString();
		headerViewHolder.userTopicTitleText.setText(htmlSafeTitle);
		
		ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(
			UserTopicActivity.this,
			headerViewHolder.topicIcon,
			headerViewHolder.unreadResponseCountText,
			headerViewHolder.totalResponseCountText,
			headerViewHolder.userResponseCountText
		);
		responseCountViewHelper.setResponseCount(responseCount);
		refreshDescriptionView();
		
		fetchResponses(false);
		restorePostIfSaved(savedInstanceState);
	}

}
