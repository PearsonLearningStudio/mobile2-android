package com.ecollege.android;

import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class ProfileActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.username) TextView username;
    @InjectView(R.id.name_text) TextView nameText;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        client = app.getClient();

        username.setText(app.getCurrentUser().getUserName());
        nameText.setText(app.getCurrentUser().getFirstName() + " " + app.getCurrentUser().getLastName());
    }
}