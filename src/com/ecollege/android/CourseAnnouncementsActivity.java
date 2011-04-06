package com.ecollege.android;

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
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.api.model.Announcement;
import com.ecollege.api.model.Course;
import com.ecollege.api.services.courses.FetchAnnouncementsForCourse;
import com.google.inject.Inject;

public class CourseAnnouncementsActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectResource(R.string.announcements) String announcementsTitle;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	@InjectExtra(value=CourseActivity.ANNOUNCEMENT_LIST_EXTRA,optional=true) List<Announcement> announcements;
	private LayoutInflater viewInflater;
	private UberAdapter<Announcement> announcementsAdapter;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		viewInflater = getLayoutInflater();
		titleText.setText(announcementsTitle);
		displayCourse();
		loadAndDisplayAnnouncementsForCourse();
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadAndDisplayAnnouncementsForCourse() {
		announcementsAdapter = new AnnouncementAdapter(this);
		setListAdapter(announcementsAdapter);
		
		if (announcements == null) {
			announcementsAdapter.beginLoading();
			buildService(new FetchAnnouncementsForCourse(course)).execute();
		} else {
			announcementsAdapter.updateItems(announcements);	
		}
	}

	public void onServiceCallSuccess(FetchAnnouncementsForCourse service) {
		announcements = service.getResult();
		announcementsAdapter.updateItems(service.getResult());
	}
	
	public void onServiceCallException(FetchAnnouncementsForCourse service, Exception ex) {
		announcementsAdapter.hasError();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		UberItem<Announcement> item = (UberItem<Announcement>)l.getItemAtPosition(position);
    	Intent i = new Intent(this,AnnouncementActivity.class);
    	i.putExtra(AnnouncementActivity.COURSE_EXTRA, course);
    	i.putExtra(AnnouncementActivity.ANNOUNCEMENT_EXTRA, item.getDataItem());
    	i.putExtra(AnnouncementActivity.FINISH_ON_CLICK_ALL_ANNOUNCEMENTS_EXTRA, true);
    	startActivity(i);
	}
	
	protected class AnnouncementViewHolder {
		public TextView titleText;
		public TextView descriptionText;
	}
	
	protected class AnnouncementAdapter extends UberAdapter<Announcement> {

		public AnnouncementAdapter(Context context) {
			super(context, false, false, false);
		}
		

		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<Announcement> item) {
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
			Announcement announcement = item.getDataItem();
			holder.titleText.setText(announcement.getSubject());
			holder.descriptionText.setText(announcement.getRawText());
			return convertView;
		}
	}
	

}
