package com.ecollege.android;

import java.util.List;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;

public class ProfileActivity extends ECollegeListActivity {
    @InjectView(R.id.username_text) TextView usernameText;
    @InjectView(R.id.name_text) TextView nameText;
    @InjectView(R.id.sign_out_button) Button signOutButton;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	private ListAdapter courseAdapter;
	private List<Course> courses;
	public LayoutInflater viewInflater;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        client = app.getClient();
        courses = app.getCurrentCourseList();
        viewInflater = getLayoutInflater();

        usernameText.setText(app.getCurrentUser().getUserName());
        nameText.setText(app.getCurrentUser().getFirstName() + " " + app.getCurrentUser().getLastName());
        
        signOutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				signOut();
			}
		});
        
        loadAndDisplayCourses();
    }

	protected void signOut() {
		app.logout();
	}
	
	protected void loadAndDisplayCourses() {
		setListAdapter(createOrReturnCourseAdapter(false));
	}
	
	protected ListAdapter createOrReturnCourseAdapter(boolean b) {
		if (courseAdapter == null) {
			courseAdapter = new CourseArrayAdapter(this, courses);
		}
		return courseAdapter;
	}
	
	protected class CourseViewHolder {
		public TextView courseTitleText;
		public TextView courseDescriptionText;
	}
	
	protected class CourseArrayAdapter extends ArrayAdapter<Course> {


		public CourseArrayAdapter(Context context, List<Course> courses) {
			super(context, 0, courses);
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			CourseViewHolder holder;
			if (convertView == null) {
				convertView = viewInflater.inflate(R.layout.course_item, null);
				holder = new CourseViewHolder();
				holder.courseTitleText = (TextView) convertView.findViewById(R.id.title_text);
				holder.courseDescriptionText = (TextView) convertView.findViewById(R.id.description_text);
				convertView.setTag(holder);
			} else {
				holder = (CourseViewHolder) convertView.getTag();
			}
			
			Course course = getItem(position);
			holder.courseTitleText.setText(Html.fromHtml(course.getTitle()));
			holder.courseDescriptionText.setText(course.getDisplayCourseCode());
			
			return convertView;
		}
		
	}

	
}