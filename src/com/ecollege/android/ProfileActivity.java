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
    @InjectView(R.id.firstname) TextView firstname;
    @InjectView(R.id.lastname) TextView lastname;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        client = app.getClient();

        username.setText("Username: " + app.getCurrentUser().getUserName());
        firstname.setText("Firstname: " + app.getCurrentUser().getFirstName());
        lastname.setText("Lastname: " + app.getCurrentUser().getLastName());
    }
}