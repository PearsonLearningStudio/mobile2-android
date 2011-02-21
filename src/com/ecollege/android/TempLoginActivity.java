package com.ecollege.android;

import com.ecollege.android.api.AndroidHttpClient;
import com.ecollege.api.SessionManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TempLoginActivity extends Activity {
    private Button loginButton;
	private EditText usernameText;
	private EditText clientStringText;
	private EditText passwordText;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loginButton = (Button)findViewById(R.id.login_button);
        usernameText = (EditText)findViewById(R.id.username_text);
        clientStringText = (EditText)findViewById(R.id.client_string_text);
        passwordText = (EditText)findViewById(R.id.password_text);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				SessionManager sessionManager = new SessionManager(
						"30bb1d4f-2677-45d1-be13-339174404402",
						clientStringText.getText().toString(),
						usernameText.getText().toString(),
						passwordText.getText().toString(),
						new AndroidHttpClient());
				sessionManager.authenticate();
				Log.d("stuff", "worked");
			}
		});
    }
}