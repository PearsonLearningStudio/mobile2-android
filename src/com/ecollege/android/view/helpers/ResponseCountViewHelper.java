package com.ecollege.android.view.helpers;

import com.ecollege.android.R;
import com.ecollege.api.model.ResponseCount;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ResponseCountViewHelper {

	private String totalResponsesFormat;
	private String totalResponseFormat;
	private String responsesByYouFormat;
	private String responseByYouFormat;
	private String noResponsesString;
	
	private ImageView topicIcon;
	private TextView unreadResponseCountText;
	private TextView totalResponseCountText;
	private TextView userResponseCountText;

	public ResponseCountViewHelper(Context context, ImageView topicIcon, TextView unreadResponseCountText, TextView totalResponseCountText, TextView userResponseCountText) {
		totalResponsesFormat = context.getString(R.string.d_total_reponses);
		totalResponseFormat = context.getString(R.string.d_total_reponse);
		responsesByYouFormat = context.getString(R.string.d_responses_by_you);
		responseByYouFormat = context.getString(R.string.d_response_by_you);
		noResponsesString = context.getString(R.string.no_responses);
		
		this.topicIcon = topicIcon;
		this.unreadResponseCountText = unreadResponseCountText;
		this.totalResponseCountText = totalResponseCountText;
		this.userResponseCountText = userResponseCountText;
	}

	public void setResponseCount(ResponseCount responseCount) {
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
