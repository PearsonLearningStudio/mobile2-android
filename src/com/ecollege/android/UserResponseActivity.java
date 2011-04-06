package com.ecollege.android;

import roboguice.inject.InjectExtra;
import android.os.Bundle;
import android.text.Html;

import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.model.DiscussionResponse;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.services.discussions.FetchDiscussionResponseById;

public class UserResponseActivity extends UserDiscussionActivity {

	public static final String RESPONSE_ID_EXTRA = "RESPONSE_ID_EXTRA";
	public static final String USER_RESPONSE_EXTRA = "USER_RESPONSE_EXTRA";
	
	@InjectExtra(value=USER_RESPONSE_EXTRA,optional=true) protected UserDiscussionResponse userResponse;
	@InjectExtra(value=RESPONSE_ID_EXTRA,optional=true) protected Long responseId;
	
	@Override
	protected UserDiscussionResponse getUserResponse() {
		return userResponse;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussion);
		
		setupView();
		
		if (userResponse == null) {
			lastSavedInstanceState = savedInstanceState;
			buildService(new FetchDiscussionResponseById(getApp().getCurrentUser().getId(), responseId)).execute();
		} else {
			populateView(savedInstanceState);
		}
	}

	public void onServiceCallSuccess(FetchDiscussionResponseById service) {
		userResponse = service.getResult();
		populateView(lastSavedInstanceState);
	}
	
	protected void populateView(Bundle savedInstanceState) {
		DiscussionResponse response = userResponse.getResponse();
		responseCount = userResponse.getChildResponseCounts();
		styledDescriptionHtml = Html.fromHtml(response.getDescription());

		headerViewHolder.parentTitleText.setText(Html.fromHtml(response.getTitle()));

		String htmlSafeTitle = Html.fromHtml(response.getTitle()).toString();
		headerViewHolder.userTopicTitleText.setText(htmlSafeTitle);
		
		ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(
			UserResponseActivity.this,
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