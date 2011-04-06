package com.ecollege.android;

import java.util.Collections;

import roboguice.inject.InjectExtra;
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
import com.ecollege.api.model.Course;
import com.ecollege.api.model.RosterUser;
import com.ecollege.api.services.users.FetchRosterService;
import com.google.inject.Inject;

public class CoursePeopleActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	private LayoutInflater viewInflater;
	private PeopleAdapter personAdapter;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		viewInflater = getLayoutInflater();
		titleText.setText(R.string.people);
		displayCourse();
		loadPeopleForCourse();
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadPeopleForCourse() {
		personAdapter = new PeopleAdapter(this);
		setListAdapter(personAdapter);
	
		personAdapter.beginLoading();
		buildService(new FetchRosterService(course.getId())).execute();
	}

	public void onServiceCallSuccess(FetchRosterService service) {
		Collections.sort(service.getResult());
		personAdapter.updateItems(service.getResult());
	}
	
	public void onServiceCallException(FetchRosterService service, Exception ex) {
		personAdapter.hasError();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		UberItem<RosterUser> item = (UberItem<RosterUser>)l.getItemAtPosition(position);
    	Intent i = new Intent(this,PersonActivity.class);
    	i.putExtra(PersonActivity.COURSE_EXTRA, course);
    	i.putExtra(PersonActivity.PERSON_EXTRA, item.getDataItem());
    	i.putExtra(PersonActivity.FINISH_ON_CLICK_ALL_PEOPLE_EXTRA, true);
    	startActivity(i);
	}
	
	protected class PersonViewHolder {
		public TextView personNameText;
		public TextView personRoleText;
	}
	
	protected class PeopleAdapter extends UberAdapter<RosterUser> {

		public PeopleAdapter(Context context) {
			super(context, true, false, false);
		}
		
		@Override
		protected Object groupIdFunction(RosterUser item) {
			return item.getLastNameFirstChar();
		}

		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<RosterUser> item) {
			PersonViewHolder holder;
			if (convertView == null) {
				holder = new PersonViewHolder();
				convertView = viewInflater.inflate(R.layout.person_list_item, null);
				holder.personNameText = (TextView) convertView.findViewById(R.id.person_name_text);
				holder.personRoleText = (TextView) convertView.findViewById(R.id.person_role_text);
				convertView.setTag(holder);
			} else {
				holder = (PersonViewHolder) convertView.getTag();
			}
			RosterUser user = item.getDataItem();
			holder.personNameText.setText(user.getDisplayName());
			holder.personRoleText.setText(user.getFriendlyRole());
			return convertView;
		}
	}
	

}
