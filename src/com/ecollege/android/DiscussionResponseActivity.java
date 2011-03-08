package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.services.discussions.FetchDiscussionResponseById;
import com.google.inject.Inject;

public class DiscussionResponseActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.main_text) TextView mainText;
	@InjectExtra("responseId") long responseId;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion_response);
        client = app.getClient();
        fetchResponse();
    }
    
    protected void fetchResponse() {
    	buildService(new FetchDiscussionResponseById(app.getCurrentUser().getId(),responseId)).execute();
    }
    
    public void onServiceCallSuccess(FetchDiscussionResponseById service) {
    	UserDiscussionResponse result = service.getResult();
    	String content = "Title: " + result.getResponse().getTitle();
    	content += "\nDescription: " + result.getResponse().getDescription();
    	mainText.setText(content);
    }
}