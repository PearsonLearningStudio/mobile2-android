package com.ecollege.android;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeActivity;
import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Announcement;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.User;
import com.ecollege.api.services.courses.FetchAnnouncementsForCourse;
import com.ecollege.api.services.courses.FetchInstructorsForCourse;
import com.google.inject.Inject;

public class CourseActivity extends ECollegeListActivity {
	
	public static final String ANNOUNCEMENT_LIST_EXTRA = "ANNOUNCEMENT_LIST_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.course_code_text) TextView courseCodeText;
	@InjectView(R.id.instructor_text) TextView instructorText;
	@InjectView(R.id.announcement_title) TextView announcementTitle;
	@InjectView(R.id.announcement_description) TextView announcementDescription;
	@InjectView(R.id.instructor_loading_indicator) ProgressBar instructorLoadingIndicator;
	@InjectView(R.id.announcement_loading_indicator) ProgressBar announcementsLoadingIndicator;
	@InjectResource(R.array.course_menu_items) String[] courseMenuItems;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	protected ECollegeClient client;
	protected LayoutInflater viewInflater;
	private CourseMenuAdapter courseMenuAdapter;
	private List<User> instructors;
	private List<Announcement> announcements;

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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Class<? extends ECollegeActivity> destinationClass;
		String detailExtraKey = null;
		Serializable detailExtraValue = null;
		switch (position) {
		case 0:
			destinationClass = AnnouncementsActivity.class;
			detailExtraKey = ANNOUNCEMENT_LIST_EXTRA;
			detailExtraValue = (Serializable) announcements;
			break;

		default:
			return;
		}
		Intent intent = new Intent(this, destinationClass);
		intent.putExtra(CoursesActivity.COURSE_EXTRA, course);
		if (detailExtraKey != null && detailExtraValue != null) {
			intent.putExtra(detailExtraKey, detailExtraValue);
		}
		startActivity(intent);
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
		courseCodeText.setText(Html.fromHtml(course.getDisplayCourseCode()));
	}

	private void createMenu() {
		courseMenuAdapter = new CourseMenuAdapter(this, courseMenuItems);
		setListAdapter(courseMenuAdapter);
	}

	private void displayInstructorList() {
		showInstructorLoadingProgress(false);
		if (!instructors.isEmpty()) {
			Iterator<User> i = instructors.listIterator();
			StringBuffer sb = new StringBuffer("");
			User instructor;
			while (i.hasNext()) {
				instructor = i.next();
				sb.append(instructor.getFirstName() + " " + instructor.getLastName());
				if (i.hasNext()) sb.append(", ");
			}
			instructorText.setText(sb.toString());
		} else {
			instructorText.setText(R.string.no_instructor);
		}
	}

	private void displayFirstAnnouncement() {
		showAnnouncementLoadingProgress(false);
		if (!announcements.isEmpty()) {
			Announcement firstAnnouncement = announcements.get(0);
			announcementTitle.setText(firstAnnouncement.getSubject());
			announcementDescription.setText(firstAnnouncement.getRawText());
		} else {
			announcementTitle.setText(R.string.no_announcements);
			announcementDescription.setText(null);
		}
	}

	private void displayAnnouncementCount() {
		courseMenuAdapter.notifyDataSetChanged();
	}

	private void loadAndDisplayInstructorsForCourse() {
		showInstructorLoadingProgress(true);
		buildService(new FetchInstructorsForCourse(course)).execute();
	}

	private void loadAndDisplayAnnouncementsForCourse() {
		showAnnouncementLoadingProgress(true);
		buildService(new FetchAnnouncementsForCourse(course, true)).execute();
	}
	
	private void showAnnouncementLoadingProgress(boolean loading) {
		if (loading) {
			announcementsLoadingIndicator.setVisibility(View.VISIBLE);
			announcementTitle.setVisibility(View.GONE);
			announcementDescription.setVisibility(View.GONE);
		} else {
			announcementsLoadingIndicator.setVisibility(View.GONE);
			announcementTitle.setVisibility(View.VISIBLE);
			announcementDescription.setVisibility(View.VISIBLE);
		}
	}

	private void showInstructorLoadingProgress(boolean loading) {
		if (loading) {
			instructorLoadingIndicator.setVisibility(View.VISIBLE);
			instructorText.setVisibility(View.GONE);
		} else {
			instructorLoadingIndicator.setVisibility(View.GONE);
			instructorText.setVisibility(View.VISIBLE);
		}
	}

	public void onServiceCallSuccess(FetchInstructorsForCourse service) {
		instructors = service.getResult();
		displayInstructorList();
	}
	
	public void onServiceCallSuccess(FetchAnnouncementsForCourse service) {
		// stop progress
		announcements = service.getResult();
		displayFirstAnnouncement();
		displayAnnouncementCount();
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
			// first position should be "Announcements"
			if (position == 0 && announcements != null && announcements.size() > 0) {
				holder.unreadCountText.setVisibility(View.VISIBLE);
				holder.unreadCountText.setText(announcements.size() + "");
			} else {
				holder.unreadCountText.setVisibility(View.GONE);
				holder.unreadCountText.setText(null);
			}
			return convertView;
		}
		
	}
}
