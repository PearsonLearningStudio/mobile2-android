package com.ecollege.android;

import roboguice.inject.InjectView;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.google.inject.Inject;

public class MainActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.username) TextView usernameLabel;
	@InjectView(R.id.firstname) TextView firstnameLabel;
	@InjectView(R.id.lastname) TextView lastnameLabel;
	@Inject ECollegeApplication app;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        usernameLabel.setText(app.getCurrentUser().getUserName());
        firstnameLabel.setText(app.getCurrentUser().getFirstName());
        lastnameLabel.setText(app.getCurrentUser().getLastName());
    }
}