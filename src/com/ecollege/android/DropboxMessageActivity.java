package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.util.DateTimeUtil;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.DropboxMessage;
import com.ecollege.api.services.dropbox.FetchDropboxMessage;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class DropboxMessageActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	
	@InjectExtra("courseId") long courseId;
	@InjectExtra("basketId") long basketId;
	@InjectExtra("messageId") long messageId;
	@InjectExtra("title") String title;
	
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.message_title_text) TextView messageTitleText;
	@InjectView(R.id.comments_text) TextView commentsText;
	@InjectView(R.id.message_text) TextView messageText;
	@InjectView(R.id.author_text) TextView authorText;
	@InjectView(R.id.date_text) TextView dateText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	@InjectResource(R.string.no_comments) String no_comments;
	@InjectResource(R.string.dropbox_comments_label) String dropboxCommentsLabel;
	
	protected ECollegeClient client;
	protected Course course;
	protected DropboxMessage message;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_message);
        client = app.getClient();
    	course = app.getCourseById(courseId);
    	displayResults();
        fetchData();
    }
    
    protected void fetchData() {
    	buildService(new FetchDropboxMessage(courseId,basketId,messageId)).execute();
    }
    
    public void onServiceCallSuccess(FetchDropboxMessage service) {
    	message = service.getResult(); 
    	displayResults();
    }

	private void displayResults() {
		if (null != course) {
			courseTitleText.setText(Html.fromHtml(course.getTitle()));
		}
		
		if (null != message) {
			messageText.setText(title);
			authorText.setText(String.format(dropboxCommentsLabel, message.getAuthor().getDisplayName()));
			commentsText.setText(Html.fromHtml(message.getComments()));
			dateText.setText(DateTimeUtil.getLongFriendlyDate(message.getDate()));
		}
	}
}