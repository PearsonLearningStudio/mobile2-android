package com.ecollege.android;

import java.util.Date;
import java.util.List;

import roboguice.inject.InjectView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.tasks.TaskPostProcessor;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.services.courses.FetchMyCoursesService;
import com.google.inject.Inject;

public class CoursesActivity extends ECollegeListActivity {
	
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.last_updated_text) TextView lastUpdatedText;
	@InjectView(R.id.reload_button) Button reloadButton;
	protected ECollegeClient client;
	protected LayoutInflater viewInflater;
	protected List<Course> courses;
	protected CourseArrayAdapter courseAdapter;
	
	protected View.OnClickListener reloadClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			reloadAndDisplayCourses();
		}
	};
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courses);
        client = app.getClient();
        viewInflater = getLayoutInflater();
        courses = app.getCurrentCourseList();
        reloadButton.setOnClickListener(reloadClickListener);
        loadAndDisplayCourses();
    }

	protected void loadAndDisplayCourses() {
		String formattedLastUpdated = getString(R.string.never);
		if (app.getCurrentCourseListLastLoaded() != 0) {
			formattedLastUpdated = new Date(app.getCurrentCourseListLastLoaded()).toString();
		}
		lastUpdatedText.setText(formattedLastUpdated);
		setListAdapter(createOrReturnCourseAdapter(false));
	}
	
	protected void reloadAndDisplayCourses() {
		buildService(new FetchMyCoursesService())
			.bypassFileCache()
			.bypassResultCache()
			.setPostProcessor(new TaskPostProcessor<FetchMyCoursesService>() {
				@Override public FetchMyCoursesService onPostProcess(FetchMyCoursesService service) {
					app.setCurrentCourseList(service.getResult());
					return service;
				}
			})
			.execute();
	}
	
	public void onServiceCallSuccess(FetchMyCoursesService service) {
		courseAdapter = null;
		loadAndDisplayCourses();
	}
	
	protected ListAdapter createOrReturnCourseAdapter(boolean b) {
		if (courseAdapter == null) {
			courseAdapter = new CourseArrayAdapter(this, courses);
		}
		return courseAdapter;
	}

	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Course selectedCourse = courseAdapter.getItem(position);
		Intent intent = new Intent(this, CourseActivity.class);
		intent.putExtra(COURSE_EXTRA, selectedCourse);
		startActivity(intent);
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