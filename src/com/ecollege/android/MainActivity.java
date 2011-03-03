package com.ecollege.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TabHost;

import com.ecollege.android.activities.ECollegeTabActivity;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.users.FetchMeService;
import com.google.inject.Inject;

public class MainActivity extends ECollegeTabActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = app.getClient();
        setContentView(R.layout.main);        	        	
        
        if (app.getCurrentUser() == null) {
	        String grantToken = prefs.getString("grantToken", null);
	        if (grantToken != null) {
	    		client.setupAuthentication(grantToken);
	        	fetchCurrentUser();
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
    
    protected void fetchCurrentUser() {		
		new ServiceCallTask<FetchMeService>(app,new FetchMeService()) {
			@Override
			protected void onSuccess(FetchMeService service) throws Exception {
				super.onSuccess(service);
				app.setCurrentUser(service.getResult());				
				
				if (currentContext.get() instanceof MainActivity) {
					((MainActivity)currentContext.get()).setupActivity();
				}
			}
		}.execute();
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
        host.addTab(host.newTabSpec("home").setIndicator("Home").setContent(i));
    }
    
    protected void addDiscussionsTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,DiscussionsActivity.class);
        host.addTab(host.newTabSpec("discussions").setIndicator("Discussions").setContent(i));
    }
    
    protected void addCoursesTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,CoursesActivity.class);
        host.addTab(host.newTabSpec("courses").setIndicator("Courses").setContent(i));
    }
    
    protected void addPeopleTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,PeopleActivity.class);
        host.addTab(host.newTabSpec("people").setIndicator("People").setContent(i));
    }
    
    protected void addProfileTab()
    {
        TabHost host = getTabHost();
        Intent i= new Intent(this,ProfileActivity.class);
        host.addTab(host.newTabSpec("profile").setIndicator("Profile").setContent(i));
    }
}