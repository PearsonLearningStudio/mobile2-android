package com.ecollege.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.users.FetchMeService;

public class LoginActivity extends ECollegeDefaultActivity {
    private Button loginButton;
	private EditText usernameText;
	private EditText passwordText;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton = (Button)findViewById(R.id.login_button);
        usernameText = (EditText)findViewById(R.id.username_text);
        passwordText = (EditText)findViewById(R.id.password_text);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ECollegeClient client = getApp().getClient();
				client.setupAuthentication(usernameText.getText().toString(), passwordText.getText().toString());
				
	        	(new ServiceCallTask<FetchMeService>(LoginActivity.this)).execute(new FetchMeService());
			}
		});
    }
    
    public void onFetchMeServiceSuccess(FetchMeService service) {
    	AlertDialog alert = new AlertDialog.Builder(this).create();
    	alert.setTitle("Login success!");
    	alert.setMessage("You are: " + service.getResult().getUserName());
    	alert.show();
    }
}