package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicById;
import com.google.inject.Inject;

public class DiscussionTopicActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.main_text) TextView mainText;
	@InjectExtra("topicId") long topicId;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion_topic);
        client = app.getClient();
        fetchTopic();
    }
    
    protected void fetchTopic() {
    	app.buildService(new FetchDiscussionTopicById(app.getCurrentUser().getId(),topicId)).execute();
    }
    
    public void onServiceCallSuccess(FetchDiscussionTopicById service) {
    	UserDiscussionTopic result = service.getResult();
    	String content = "Title: " + result.getTopic().getTitle();
    	content += "\nDescription: " + result.getTopic().getDescription();
    	mainText.setText(content);
    }
}