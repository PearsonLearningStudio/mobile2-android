package com.ecollege.android;

import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class ProfileActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.username_text) TextView usernameText;
    @InjectView(R.id.name_text) TextView nameText;
    @InjectView(R.id.sign_out_button) Button signOutButton;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        client = app.getClient();

        usernameText.setText(app.getCurrentUser().getUserName());
        nameText.setText(app.getCurrentUser().getFirstName() + " " + app.getCurrentUser().getLastName());
        
        signOutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				signOut();
			}
		});
    }

	protected void signOut() {
		app.logout();
	}
}