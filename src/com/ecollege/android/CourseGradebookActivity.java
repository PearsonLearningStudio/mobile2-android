package com.ecollege.android;

import java.util.ArrayList;
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
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.LoadMoreAdapter;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.Grade;
import com.ecollege.api.model.GradebookItem;
import com.ecollege.api.model.UserGradebookItem;
import com.ecollege.api.services.courses.FetchUserGradebookItemsForCourseId;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class CourseGradebookActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.no_data_label) TextView noDataLabel;
	@InjectResource(R.string.gradebook) String gradebookTitleResource;
	@InjectResource(R.string.no_grade) String noGradeResource;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	private LayoutInflater viewInflater;
	private LoadMoreAdapter gradebookLoadMoreAdapter;
	
	private static final PrettyTime prettyTimeFormatter = new PrettyTime();
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		viewInflater = getLayoutInflater();
		titleText.setText(gradebookTitleResource);
		noDataLabel.setText(R.string.no_gradebook_items);
		displayCourse();
		loadAndDisplayUserGradebookItems();
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadAndDisplayUserGradebookItems() {
		if (gradebookLoadMoreAdapter == null) {
			gradebookLoadMoreAdapter = new LoadMoreAdapter(this, new UserGradebookItemAdapter(this, new ArrayList<UserGradebookItem>()), true);
			gradebookLoadMoreAdapter.setIsLoadingMore(true);
			buildService(new FetchUserGradebookItemsForCourseId(course.getId(), true)).execute();
		}
		setListAdapter(gradebookLoadMoreAdapter);
	}

    public void onServiceCallSuccess(FetchUserGradebookItemsForCourseId service) {
    	UserGradebookItemAdapter newAdapter = new UserGradebookItemAdapter(this, service.getResult());
    	gradebookLoadMoreAdapter.update(newAdapter, false);
    }
    
	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		UserGradebookItem item = (UserGradebookItem) getListAdapter().getItem(position);
		if (item.getGrade() != null) {
			GradebookItem gradebookItem = item.getGradebookItem();
	    	Intent i = new Intent(this,GradeActivity.class);
	    	i.putExtra("courseId", course.getId());
	    	i.putExtra("gradebookItemGuid", gradebookItem.getId());
	    	i.putExtra(GradeActivity.FINISH_ON_CLICK_ALL_GRADES_EXTRA, true);
	    	startActivity(i);
		}
	}

	protected class UserGradebookItemHolder {
		public TextView titleText;
		public TextView gradeText;
		public TextView dateText;
	}
	
	protected class UserGradebookItemAdapter extends ArrayAdapter<UserGradebookItem> {
		
		public UserGradebookItemAdapter (Context context, List<UserGradebookItem> list) {
			super(context, 0, list);
		}
		
		@Override public View getView(int position, View convertView, ViewGroup parent) {
			UserGradebookItemHolder holder;
			if (convertView == null) {
				holder = new UserGradebookItemHolder();
				convertView = viewInflater.inflate(R.layout.gradebook_item, null);
				holder.titleText = (TextView) convertView.findViewById(R.id.item_title);
				holder.dateText = (TextView) convertView.findViewById(R.id.date_text);
				holder.gradeText = (TextView) convertView.findViewById(R.id.grade_text);
				convertView.setTag(holder);
			} else {
				holder = (UserGradebookItemHolder) convertView.getTag();
			}
			UserGradebookItem ugbi = getItem(position);
			GradebookItem gradebookItem = ugbi.getGradebookItem();
			Grade grade = ugbi.getGrade();
			holder.titleText.setText(gradebookItem.getTitle());
			if (null == grade) {
				holder.dateText.setText(noGradeResource);
				holder.gradeText.setText(null);
			} else {
				holder.dateText.setText(prettyTimeFormatter.format(grade.getUpdatedDate().getTime()));
				holder.gradeText.setText(grade.getLetterGrade());
			}
			return convertView;
		}
	}
}
