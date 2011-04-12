package com.ecollege.android;

import roboguice.inject.InjectResource;
import roboguice.util.Ln;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.courses.FetchMyCoursesService;
import com.ecollege.api.services.users.FetchMeService;
import com.google.inject.Inject;

public class SplashActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	private boolean meLoaded;
	private boolean coursesLoaded;
	
	@InjectResource(R.string.use_sso) String use_sso;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);        	
        client = app.getClient();
        
        if (app.getCurrentUser() == null) {
	        String grantToken = prefs.getString("grantToken", null);
	        if (grantToken != null) {
	    		client.setupAuthentication(grantToken);
	        	fetchCurrentUserAndCourses();
	        } else {
	        	if (use_sso.equals("true")) {
		        	Intent myIntent = new Intent(this, SingleSignonActivity.class);
		        	startActivityForResult(myIntent, SSO_LOGIN_REQUEST_CODE);
	        	} else {
		        	Intent myIntent = new Intent(this, LoginActivity.class);
		        	startActivityForResult(myIntent, LOGIN_REQUEST_CODE);
	        	}
	        }        	
        } else {
        	fetchCurrentUserAndCourses();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
    		fetchCurrentUserAndCourses();
    	} else if (requestCode == SSO_LOGIN_REQUEST_CODE && resultCode == RESULT_OK){
    		fetchCurrentUserAndCourses();
    	} else {
    		finish();
    	}
    }
    
    protected void fetchCurrentUserAndCourses() {
    	buildService(new FetchMeService()).execute();
    	buildService(new FetchMyCoursesService()).execute();
    }    
    
    public void onServiceCallSuccess(FetchMeService service) {
		app.setCurrentUser(service.getResult());	
    	Ln.i("User loaded from the Splash activity");
		meLoaded = true;
		showMainActivityIfServiceCallsAreComplete();
    }
    
    public void onServiceCallSuccess(FetchMyCoursesService service) {
    	app.setCurrentCourseList(service.getResult());
    	Ln.i("Courses loaded from the Splash activity");
    	coursesLoaded = true;
    	showMainActivityIfServiceCallsAreComplete();
    }
    
    protected void showMainActivityIfServiceCallsAreComplete() {
    	if (meLoaded && coursesLoaded) {
    		showMainActivity();
    	}
    }
    
    protected void showMainActivity() {
    	Intent i = new Intent(this,MainActivity.class);
    	startActivityForResult(i,MAIN_ACTIVITY_REQUEST_CODE);
    }
}