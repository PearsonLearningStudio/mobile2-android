package com.ecollege.android;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.tasks.TaskPostProcessor;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.services.courses.FetchMyCoursesService;
import com.google.inject.Inject;

public class CoursesActivity extends ECollegeListActivity {
	
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
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
        loadAndDisplayCourses(app.getCurrentCourseListLastLoaded());
    }

	protected void loadAndDisplayCourses(long lastUpdatedAt) {
		if (courseAdapter == null) courseAdapter = new CourseArrayAdapter(this);
		courseAdapter.updateItems(courses);
		courseAdapter.setLastUpdatedAt(lastUpdatedAt);
		setListAdapter(courseAdapter);
	}
	
	protected void reloadAndDisplayCourses() {
		courseAdapter.beginLoading();
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
		loadAndDisplayCourses(service.getCompletedAt());
	}
	
	public void onServiceCallException(FetchMyCoursesService service, Exception ex) {
		courseAdapter.hasError();
	}

	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		UberItem<Course> item = (UberItem<Course>)l.getItemAtPosition(position);
		Intent intent = new Intent(this, CourseActivity.class);
		intent.putExtra(COURSE_EXTRA, item.getDataItem());
		startActivity(intent);
	}

	protected class CourseViewHolder {
		public TextView courseTitleText;
		public TextView courseDescriptionText;
	}
	
	protected class CourseArrayAdapter extends UberAdapter<Course> {


		public CourseArrayAdapter(Context context) {
			super(context,false,false,false);
		}
		
		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<Course> item) {
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
			
			Course course = item.getDataItem();
			holder.courseTitleText.setText(Html.fromHtml(course.getTitle()));
			holder.courseDescriptionText.setText(course.getDisplayCourseCode());
			
			return convertView;
		}
		
	}
	
}