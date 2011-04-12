package com.ecollege.android;

import roboguice.inject.InjectResource;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import com.ecollege.android.activities.ECollegeTabActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class MainActivity extends ECollegeTabActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	
	@InjectResource(R.string.use_sso) String use_sso;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        	
        client = app.getClient();
		setupActivity();
    }
    
    protected void setupActivity() {
        addHomeTab();
        addDiscussionsTab();
        addCoursesTab();
        addProfileTab();
    }
    
	protected void addHomeTab() {
		TabHost host = getTabHost();
        Intent i= new Intent(this,HomeActivity.class);
        View v = getLayoutInflater().inflate(R.layout.home_tab_view, null);
        host.addTab(host.newTabSpec("home").setIndicator(v).setContent(i));
    }
    
    protected void addDiscussionsTab() {
        TabHost host = getTabHost();
        Intent i= new Intent(this,DiscussionsActivity.class);
        View v = getLayoutInflater().inflate(R.layout.discussions_tab_view, null);
        host.addTab(host.newTabSpec("discussions").setIndicator(v).setContent(i));
    }
    
    protected void addCoursesTab() {
        TabHost host = getTabHost();
        Intent i= new Intent(this,CoursesActivity.class);
        View v = getLayoutInflater().inflate(R.layout.courses_tab_view, null);
        host.addTab(host.newTabSpec("courses").setIndicator(v).setContent(i));
    }
    
    protected void addProfileTab() {
        TabHost host = getTabHost();
        Intent i= new Intent(this,ProfileActivity.class);
        View v = getLayoutInflater().inflate(R.layout.profile_tab_view, null);
        host.addTab(host.newTabSpec("profile").setIndicator(v).setContent(i));
    }
}