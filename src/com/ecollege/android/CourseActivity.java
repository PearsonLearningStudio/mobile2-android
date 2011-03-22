package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ecollege.android.CourseActivity.CourseMenuAdapter;
import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;

public class CourseActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.course_title) TextView courseTitleText;
	@InjectView(R.id.instructor_text) TextView instructorText;
	@InjectResource(R.array.course_menu_items) String[] courseMenuItems;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	protected ECollegeClient client;
	protected LayoutInflater viewInflater;
	private CourseMenuAdapter courseMenuAdapter;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course);
		
		viewInflater = getLayoutInflater();
		client = app.getClient();
		displayCourse();
		createMenu();
		loadAndDisplayInstructorsForCourse();
		loadAndDisplayAnnouncementsForCourse();
	}
	
	private void displayCourse() {
		courseTitleText.setText(course.getTitle());
		instructorText.setText("Bob Dobbs");
	}

	private void createMenu() {
		courseMenuAdapter = new CourseMenuAdapter(this, courseMenuItems);
		setListAdapter(courseMenuAdapter);
	}

	private void loadAndDisplayInstructorsForCourse() {
	}

	private void loadAndDisplayAnnouncementsForCourse() {
	}
	
	protected class CourseMenuItemViewHolder {
		public TextView title;
		public TextView unreadCountText;
	}
	
	protected class CourseMenuAdapter extends ArrayAdapter<String> {

		public CourseMenuAdapter(Context context, String[] objects) {
			super(context, 0, objects);
		}
		
		@Override public View getView(int position, View convertView, ViewGroup parent) {
			CourseMenuItemViewHolder holder;
			if (convertView == null) {
				holder = new CourseMenuItemViewHolder();
				convertView = viewInflater.inflate(R.layout.course_menu_item, null);
				holder.title = (TextView) convertView.findViewById(R.id.course_title);
				holder.unreadCountText = (TextView) convertView.findViewById(R.id.unread_count_text);
				convertView.setTag(holder);
			} else {
				holder = (CourseMenuItemViewHolder) convertView.getTag();
			}
			holder.title.setText(getItem(position));
			return convertView;
		}
		
	}
}
