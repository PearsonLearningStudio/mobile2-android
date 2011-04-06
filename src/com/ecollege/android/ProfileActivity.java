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
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;

public class ProfileActivity extends ECollegeListActivity {
    @InjectView(R.id.name_text) TextView nameText;
    @InjectView(R.id.sign_out_button) Button signOutButton;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	protected ECollegeClient client;
	private CourseArrayAdapter courseAdapter;
	private List<Course> courses;
	public LayoutInflater viewInflater;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        client = app.getClient();
        courses = app.getCurrentCourseList();
        viewInflater = getLayoutInflater();

        nameText.setText(app.getCurrentUser().getFirstName() + " " + app.getCurrentUser().getLastName());
        
        signOutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				signOut();
			}
		});
        
        courseAdapter = new CourseArrayAdapter(this);
        courseAdapter.updateItems(courses);
        setListAdapter(courseAdapter);
    }

	protected void signOut() {
		app.logout();
	}

	protected class CourseViewHolder {
		public TextView courseTitleText;
		public TextView courseDescriptionText;
	}
	
	protected class CourseArrayAdapter extends UberAdapter<Course> {

		public CourseArrayAdapter(Context context) {
			super(context, false, false, false);
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		@Override
		public boolean isEnabled(int position) {
			return false;
		}
		
		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<Course> item) {
			CourseViewHolder holder;
			if (convertView == null) {
				convertView = viewInflater.inflate(R.layout.simple_course_list_item, null);
				holder = new CourseViewHolder();
				holder.courseTitleText = (TextView) convertView.findViewById(R.id.course_title);
				holder.courseDescriptionText = (TextView) convertView.findViewById(R.id.course_code_text);
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