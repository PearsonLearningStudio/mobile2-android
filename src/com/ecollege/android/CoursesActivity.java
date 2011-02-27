package com.ecollege.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.FetchGrantService;

public class CoursesActivity extends ECollegeListActivity {
    private Button loginButton;
	private EditText usernameText;
	private EditText clientStringText;
	private EditText passwordText;
	private TextView resultText;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton = (Button)findViewById(R.id.login_button);
        usernameText = (EditText)findViewById(R.id.username_text);
        passwordText = (EditText)findViewById(R.id.password_text);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				ECollegeClient client = new ECollegeClient(clientStringText.getText().toString(), "30bb1d4f-2677-45d1-be13-339174404402");
				
				FetchGrantService fgs = new FetchGrantService(usernameText.getText().toString(), passwordText.getText().toString());
				
				try {
					client.executeService(fgs);
					resultText.setText(fgs.getResult().getAccessToken());
					Log.d("stuff", "worked");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
    }
}