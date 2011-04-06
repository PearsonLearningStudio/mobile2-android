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
import com.ecollege.api.model.Announcement;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;

public class AnnouncementActivity extends ECollegeDefaultActivity {
	
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	public static final String ANNOUNCEMENT_EXTRA = "ANNOUNCEMENT_EXTRA";
	public static final String FINISH_ON_CLICK_ALL_ANNOUNCEMENTS_EXTRA = "FINISH_ON_CLICK_ALL_ANNOUNCEMENTS_EXTRA";
	
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra(COURSE_EXTRA) Course course;
	@InjectExtra(ANNOUNCEMENT_EXTRA) Announcement announcement;
	@InjectExtra(value = FINISH_ON_CLICK_ALL_ANNOUNCEMENTS_EXTRA, optional = true) boolean finishOnClickAllAnnouncements;
	
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.announcement_subject_text) TextView announcementSubjectText;
	@InjectView(R.id.announcement_description_text) TextView announcementDescriptionText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.announcement);
        client = app.getClient();
    	updateText();
    	
    	viewAllButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				viewAllCourseAnnouncements();
			}
		});
    }
    
    protected void viewAllCourseAnnouncements() {
    	if (finishOnClickAllAnnouncements) {
    		finish();
    	} else {
    		Intent intent = new Intent(this, CourseAnnouncementsActivity.class);
    		intent.putExtra(CoursesActivity.COURSE_EXTRA, course);
    		startActivity(intent);
    	}
	}

    protected void updateText() {
    	courseTitleText.setText(Html.fromHtml(course.getTitle()));
    	announcementSubjectText.setText(Html.fromHtml(announcement.getSubject()).toString());
    	announcementDescriptionText.setText(Html.fromHtml(announcement.getText()));
    }
}