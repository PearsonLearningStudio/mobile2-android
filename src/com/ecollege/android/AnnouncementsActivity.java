package com.ecollege.android;

import java.util.ArrayList;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.api.model.Announcement;
import com.ecollege.api.model.Course;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class AnnouncementsActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.no_data_label) TextView noDataLabel;
	@InjectResource(R.string.announcements) String announcementsTitle;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	@InjectExtra(CourseActivity.ANNOUNCEMENT_LIST_EXTRA) ArrayList<Announcement> announcements;
	private LayoutInflater viewInflater;
	
	private static PrettyTime prettyTimeFormatter = new PrettyTime();
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		viewInflater = getLayoutInflater();
		titleText.setText(announcementsTitle);
		noDataLabel.setText("No Announcements");
		displayCourse();
		loadAndDisplayAnnouncementsForCourse();
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadAndDisplayAnnouncementsForCourse() {
		setListAdapter(new AnnouncementAdapter(this, announcements));
	}

	protected class AnnouncementViewHolder {
		public TextView titleText;
		public TextView descriptionText;
	}
	
	protected class AnnouncementAdapter extends ArrayAdapter<Announcement> {
		
		public AnnouncementAdapter (Context context, ArrayList<Announcement> objects) {
			super(context, 0, objects);
		}
		
		@Override public View getView(int position, View convertView, ViewGroup parent) {
			AnnouncementViewHolder holder;
			if (convertView == null) {
				holder = new AnnouncementViewHolder();
				convertView = viewInflater.inflate(R.layout.announcement_list_item, null);
				holder.titleText = (TextView) convertView.findViewById(R.id.announcement_title);
				holder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
				convertView.setTag(holder);
			} else {
				holder = (AnnouncementViewHolder) convertView.getTag();
			}
			Announcement announcement = getItem(position);
			holder.titleText.setText(announcement.getSubject());
			holder.descriptionText.setText(announcement.getRawText());
			return convertView;
		}
	}
	

}
