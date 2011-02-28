package com.ecollege.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.ecollege.android.activities.ECollegeTabActivity;
import com.google.inject.Inject;

public class MainActivity extends ECollegeTabActivity {
	@Inject ECollegeApplication app;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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