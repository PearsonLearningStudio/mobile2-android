package com.ecollege.android;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class DiscussionsActivity extends ECollegeListActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussions);
        client = app.getClient();
    }
}