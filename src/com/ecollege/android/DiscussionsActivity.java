package com.ecollege.android;

import java.util.ArrayList;
import java.util.HashMap;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.TopicsAdapter;
import com.ecollege.android.adapter.TopicsAdapter.CourseGroupId;
import com.ecollege.android.adapter.TopicsAdapter.TopicAdapterMode;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.adapter.UberItem.UberItemType;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionTopicsForCourseIds;
import com.google.inject.Inject;

public class DiscussionsActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.reload_button) Button reloadButton;
	@InjectView(R.id.course_dropdown) Spinner courseDropdown;
	
	protected ECollegeClient client;
	private TopicsAdapter topicAdapter;
	LayoutInflater viewInflater;
	private ArrayList<String> courseDropdownTitles;
	private HashMap<String, Course> courseTitleToCourseMap;
	private long selectedCourseId;
	private boolean firstLoadFinished = false;
	
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussions);
        client = app.getClient();
        viewInflater = getLayoutInflater();
     
        loadCourseTitles();
        configureControls();

        topicAdapter = new TopicsAdapter(this,TopicAdapterMode.GROUP_BY_COURSE_FILTER_INACTIVE);
        setListAdapter(topicAdapter);
		updateCurrentTopics(false);
    }
    
    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == UserTopicActivity.VIEW_TOPIC_REQUEST) {
    		updateCurrentTopics(true);
    	}
    }
    
	private void loadCourseTitles() {
        courseDropdownTitles = new ArrayList<String>();
        courseTitleToCourseMap = new HashMap<String, Course>();
        courseDropdownTitles.add(getString(R.string.all_courses));
        String courseTitle;
        for (Course course : app.getCurrentCourseList()) {
        	courseTitle = Html.fromHtml(course.getTitle()).toString();
        	courseDropdownTitles.add(courseTitle);
        	courseTitleToCourseMap.put(courseTitle, course);
        }
	}

	private void configureControls() {
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateCurrentTopics(true);
			}
		});
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, R.layout.transparent_spinner_text_view, courseDropdownTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseDropdown.setAdapter(adapter);
		courseDropdown.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				courseSelected(arg2);
			}

			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}

	protected void courseSelected(int position) {
		if (firstLoadFinished) {
			if (position == 0) {
				selectedCourseId = 0;
			} else {
				String title = courseDropdownTitles.get(position);
				selectedCourseId = courseTitleToCourseMap.get(title).getId();
			}
			updateCurrentTopics(false);
		}
	}

	private void updateCurrentTopics(boolean reload) {
		topicAdapter.beginLoading();
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		ArrayList<String> courseIds = getSelectedCourseId();
		buildService(new FetchDiscussionTopicsForCourseIds(courseIds))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallException(FetchDiscussionTopicsForCourseIds service, Exception ex) {
		firstLoadFinished = true;
		topicAdapter.hasError();
	}
	
	public void onServiceCallSuccess(FetchDiscussionTopicsForCourseIds service) {
		firstLoadFinished = true;
		topicAdapter.setLastUpdatedAt(service.getCompletedAt());
		topicAdapter.updateItems(service.getResult());
	}

	private ArrayList<String> getSelectedCourseId() {
		boolean allCoursesSelected = (selectedCourseId == 0);
		ArrayList<String> ids = new ArrayList<String>();
		if (allCoursesSelected) { // all courses selected
			for (Course course : app.getCurrentCourseList()) {
				ids.add(Long.toString(course.getId()));
			}
		} else {
			ids.add(Long.toString(selectedCourseId));
		}
		return ids;
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		@SuppressWarnings("unchecked")
		UberItem<UserDiscussionTopic> item = (UberItem<UserDiscussionTopic>)l.getItemAtPosition(position);
		
		if (item.getItemType() == UberItemType.FOOTER) {
			CourseGroupId groupId = (CourseGroupId) item.getGroupId();
			long courseId = groupId.getCourseId();

			Intent intent = new Intent(this, CourseDiscussionsActivity.class);
			intent.putExtra(CourseDiscussionsActivity.COURSE_ID_EXTRA, courseId);
			startActivity(intent);
		} else {
			UserDiscussionTopic selectedTopic = item.getDataItem();
			Intent intent = new Intent(this, UserTopicActivity.class);
			intent.putExtra(UserTopicActivity.USER_TOPIC_EXTRA, selectedTopic);
			startActivityForResult(intent, UserTopicActivity.VIEW_TOPIC_REQUEST);
		}
	}
    

}