package com.ecollege.android;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
	@InjectView(R.id.remember_check) CheckBox rememberCheck;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = app.getClient();
        setContentView(R.layout.login);
        
        String grantToken = prefs.getString("grantToken", null);
        if (grantToken != null) {
    		client.setupAuthentication(grantToken);
        	fetchCurrentUser();
        }
    }
    
    public void onLoginClick(View v)
    {	
		client.setupAuthentication(usernameText.getText().toString(), passwordText.getText().toString());

		if (!rememberCheck.isChecked()) {
			if (prefs.contains("grantToken")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("grantToken");
				editor.commit(); //change to apply if android 2.2
			}
		}
		
    	fetchCurrentUser();
    }
    
    protected void fetchCurrentUser() {		
		
		new ServiceCallTask<FetchMeService>(app,new FetchMeService()) {
			@Override
			protected void onSuccess(FetchMeService service) throws Exception {
				super.onSuccess(service);
				
				if (rememberCheck.isChecked()) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("grantToken",client.getGrantToken());
					editor.commit(); //change to apply if android 2.2
				}
				
				app.setCurrentUser(service.getResult());
				
				Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivityForResult(myIntent, 0);
			}
		}.makeModal().execute();
    }
}