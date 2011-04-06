package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.RosterUser;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;

public class PersonActivity extends ECollegeDefaultActivity {
	
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	public static final String PERSON_EXTRA = "PERSON_EXTRA";
	public static final String FINISH_ON_CLICK_ALL_PEOPLE_EXTRA = "FINISH_ON_CLICK_ALL_PEOPLE_EXTRA";
	
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra(COURSE_EXTRA) Course course;
	@InjectExtra(PERSON_EXTRA) RosterUser person;
	@InjectExtra(value = FINISH_ON_CLICK_ALL_PEOPLE_EXTRA, optional = true) boolean finishOnClickAllPeople;
	
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.person_name_text) TextView personNameText;
	@InjectView(R.id.person_role_text) TextView personRoleText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        client = app.getClient();
    	updateText();
    	
    	viewAllButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				viewAllCourseRosterUsers();
			}
		});
    }
    
    protected void viewAllCourseRosterUsers() {
    	if (finishOnClickAllPeople) {
    		finish();
    	} else {
    		Intent intent = new Intent(this, CoursePeopleActivity.class);
    		intent.putExtra(CoursesActivity.COURSE_EXTRA, course);
    		startActivity(intent);
    	}
	}

    protected void updateText() {
    	courseTitleText.setText(Html.fromHtml(course.getTitle()));
    	personNameText.setText(person.getDisplayName());
    	personRoleText.setText(person.getFriendlyRole());
    }
}