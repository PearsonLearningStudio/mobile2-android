package com.ecollege.android;

import roboguice.util.Ln;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TabHost;

import com.ecollege.android.activities.ECollegeTabActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.courses.FetchMyCoursesService;
import com.ecollege.api.services.users.FetchMeService;
import com.google.inject.Inject;

public class MainActivity extends ECollegeTabActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	private boolean meLoaded;
	private boolean coursesLoaded;
	
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        	
        client = app.getClient();
        
        if (app.getCurrentUser() == null) {
	        String grantToken = prefs.getString("grantToken", null);
	        if (grantToken != null) {
	    		client.setupAuthentication(grantToken);
	        	fetchCurrentUserAndCourses();
	        } else {
	        	Intent myIntent = new Intent(this, LoginActivity.class);
	        	startActivityForResult(myIntent, LOGIN_REQUEST_CODE);
	        }        	
        } else {
        	setupActivity();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
    		setupActivity();
    	}
    }
    
    protected void fetchCurrentUserAndCourses() {
    	buildService(new FetchMeService()).execute();
    	buildService(new FetchMyCoursesService()).execute();
    }    
    
    public void onServiceCallSuccess(FetchMeService service) {
		app.setCurrentUser(service.getResult());	
    	Ln.i("User loaded from the Main activity");
		meLoaded = true;
		setupActivityIfServiceCallsAreComplete();
    }
    
    public void onServiceCallSuccess(FetchMyCoursesService service) {
    	app.setCurrentCourseList(service.getResult());
    	Ln.i("Courses loaded from the Main activity");
    	coursesLoaded = true;
    	setupActivityIfServiceCallsAreComplete();
    }
    
    protected void setupActivityIfServiceCallsAreComplete() {
    	if (meLoaded && coursesLoaded) {
    		setupActivity();
    	}
    }
    
    protected void setupActivity() {
        addHomeTab();
        addDiscussionsTab();
        addCoursesTab();
        addPeopleTab();
        addProfileTab();
    }
    
    protected void addHomeTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,HomeActivity.class);
        Drawable icon = getResources().getDrawable(R.drawable.ic_tab_home);
        host.addTab(host.newTabSpec("home").setIndicator(null,icon).setContent(i));
    }
    
    protected void addDiscussionsTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,DiscussionsActivity.class);
        Drawable icon = getResources().getDrawable(R.drawable.ic_tab_discussions);
        host.addTab(host.newTabSpec("discussions").setIndicator(null,icon).setContent(i));
    }
    
    protected void addCoursesTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,CoursesActivity.class);
        Drawable icon = getResources().getDrawable(R.drawable.ic_tab_courses);
        host.addTab(host.newTabSpec("courses").setIndicator(null,icon).setContent(i));
    }
    
    protected void addPeopleTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,PeopleActivity.class);
        Drawable icon = getResources().getDrawable(R.drawable.ic_tab_people);
        host.addTab(host.newTabSpec("people").setIndicator(null,icon).setContent(i));
    }
    
    protected void addProfileTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,ProfileActivity.class);
        Drawable icon = getResources().getDrawable(R.drawable.ic_tab_profile);
        host.addTab(host.newTabSpec("profile").setIndicator(null,icon).setContent(i));
    }
}