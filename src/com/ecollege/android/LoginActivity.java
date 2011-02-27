package com.ecollege.android;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.users.FetchMeService;
import com.google.inject.Inject;

public class LoginActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.login_button) Button loginButton;
	@InjectView(R.id.username_text) EditText usernameText;
	@InjectView(R.id.password_text) EditText passwordText;
	@Inject ECollegeApplication app;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ECollegeClient client = app.getClient();
				client.setupAuthentication(usernameText.getText().toString(), passwordText.getText().toString());
				
	        	(new ServiceCallTask<FetchMeService>(LoginActivity.this))
	        		.execute(new FetchMeService());
			}
		});
    }
    
    public void onFetchMeServiceSuccess(FetchMeService service) {
    	AlertDialog alert = new AlertDialog.Builder(this).create();
    	alert.setTitle("Login success!!!");
    	alert.setMessage("You are: " + service.getResult().getFirstName() + " " + service.getResult().getLastName());
    	alert.show();
    }
}