package com.ecollege.android;

import roboguice.inject.InjectView;
import android.app.Activity;
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
import com.google.inject.internal.Nullable;

public class LoginActivity extends ECollegeDefaultActivity {
    @Nullable @InjectView(R.id.login_button) Button loginButton;
    @Nullable @InjectView(R.id.username_text) EditText usernameText;
    @Nullable @InjectView(R.id.password_text) EditText passwordText;
    @Nullable @InjectView(R.id.remember_check) CheckBox rememberCheck;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = app.getClient();
        setContentView(R.layout.login);
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
				
				if (rememberCheck != null && rememberCheck.isChecked()) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("grantToken",client.getGrantToken());
					editor.commit(); //change to apply if android 2.2
				}
				
				app.setCurrentUser(service.getResult());				
				
				Activity a = (Activity)currentActivity.get();
				a.setResult(RESULT_OK);
				a.finish();
			}
		}.makeModal().execute();
    }
}