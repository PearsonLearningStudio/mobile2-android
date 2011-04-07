package com.ecollege.android;

import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.UberAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.RosterUser;
import com.ecollege.api.services.users.FetchRosterService;
import com.google.inject.Inject;

public class CoursePeopleActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectView(R.id.role_dropdown) Spinner roleDropdown;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	@InjectResource(R.string.all_roles) String allRoles;
	@InjectResource(R.string.student_role) String studentRole;
	@InjectResource(R.string.instructor_role) String instructorRole;
	
	private LayoutInflater viewInflater;
	private PeopleAdapter personAdapter;
	private OnItemSelectedListener onRoleSelected;
	private String currentRoleFilter;
	private List<RosterUser> allPeopleInCourse;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.people);
		
		viewInflater = getLayoutInflater();
		titleText.setText(R.string.people);
		
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				loadPeopleForCourse(true);
			}
		});
		
		displayCourse();
		loadPeopleForCourse(false);
		setupRoleFilter();
	}

	private void setupRoleFilter() {
		currentRoleFilter = allRoles;
		String[] roles = new String[] {allRoles,studentRole,instructorRole};
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, R.layout.transparent_spinner_text_view, roles);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roleDropdown.setAdapter(adapter);
		
		onRoleSelected = new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentRoleFilter = (String)parent.getItemAtPosition(position);				
				updateCurrentPeople();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				currentRoleFilter = allRoles;
				updateCurrentPeople();
			}
			
		};
		
	}

	private void updateCurrentPeople() {
		if (allPeopleInCourse != null) {
			if (currentRoleFilter.equals(allRoles)) {
				personAdapter.updateItems(allPeopleInCourse);
			} else {
				List<RosterUser> filteredPeople = new ArrayList<RosterUser>();
				for (int i=0;i<allPeopleInCourse.size();i++) {
					RosterUser ru = allPeopleInCourse.get(i);
					if (currentRoleFilter.equals(instructorRole)) {
						if (RosterUser.ROLE_CODE_INSTRUCTOR.equals(ru.getRoleCode())) {
							filteredPeople.add(ru);
						}						
					} else if (currentRoleFilter.equals(studentRole)) {
						if (RosterUser.ROLE_CODE_STUDENT.equals(ru.getRoleCode())) {
							filteredPeople.add(ru);
						}
					}
				}
				personAdapter.updateItems(filteredPeople);
			}
		}
	}
	
	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadPeopleForCourse(boolean reload) {
		personAdapter = new PeopleAdapter(this);
		setListAdapter(personAdapter);

		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;		
		
		personAdapter.beginLoading();
		buildService(new FetchRosterService(course.getId())).configureCaching(cacheConfiguration).execute();
	}

	public void onServiceCallSuccess(FetchRosterService service) {
		roleDropdown.setOnItemSelectedListener(onRoleSelected);
		personAdapter.setLastUpdatedAt(service.getCompletedAt());
		allPeopleInCourse = service.getResult();
		Collections.sort(allPeopleInCourse);
		updateCurrentPeople();
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
