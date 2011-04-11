package com.ecollege.android;

import org.apache.commons.lang.StringUtils;

import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.errors.ECollegeAlertException;
import com.ecollege.android.errors.ECollegePromptException;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.exceptions.IncorrectCredentialsException;
import com.ecollege.api.services.courses.FetchMyCoursesService;
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
	private boolean coursesLoaded;
	private boolean meLoaded;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = app.getClient();
        setTitle("PEARSON");
        setContentView(R.layout.login);
    }
    
    public void onLoginClick(View v)
    {	
    	if (StringUtils.isBlank(usernameText.getText().toString()) || StringUtils.isBlank(passwordText.getText().toString())) {
    		app.reportError(new ECollegePromptException(this, R.string.e_no_login_provided));
    		return;
    	}
    	
		client.setupAuthentication(usernameText.getText().toString(), passwordText.getText().toString());

		if (!rememberCheck.isChecked()) {
			if (prefs.contains("grantToken")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("grantToken");
				editor.commit(); //change to apply if android 2.2
			}
		}

    	buildService(new FetchMeService()).makeModal().execute();
    	buildService(new FetchMyCoursesService()).execute();
    }
    
    public boolean onServiceCallException(FetchMeService service, Exception e) {
    	if (e instanceof IncorrectCredentialsException) {
			app.reportError(new ECollegeAlertException(this, R.string.e_invalid_login, e));	
			return true; //handled
    	}
    	return false;
    }
    
    public void onServiceCallSuccess(FetchMeService service) {
    	
		if (rememberCheck != null && rememberCheck.isChecked()) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("grantToken",client.getGrantToken());
			editor.commit(); //change to apply if android 2.2
		}
		
		app.setCurrentUser(service.getResult());
    	Ln.i("User loaded from the Login activity");
		meLoaded = true;
		serviceCallComplete();
    }
    
    public void onServiceCallSuccess(FetchMyCoursesService service) {
    	app.setCurrentCourseList(service.getResult());
    	Ln.i("Courses loaded from the Login activity");
    	coursesLoaded = true;
    	serviceCallComplete();
    }

	private void serviceCallComplete() {
		if (meLoaded && coursesLoaded) {
			setResult(RESULT_OK);
			finish();
		}
	}
}