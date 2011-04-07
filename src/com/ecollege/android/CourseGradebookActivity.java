package com.ecollege.android;

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
	@InjectResource(R.string.gradebook) String gradebookTitleResource;
	@InjectResource(R.string.no_grade) String noGradeResource;
	@InjectExtra(CoursesActivity.COURSE_EXTRA) Course course;
	private LayoutInflater viewInflater;
	private UserGradebookItemAdapter gradebookAdapter;
	
	private static final PrettyTime prettyTimeFormatter = new PrettyTime();
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_menu_item_detail_view);
		
		viewInflater = getLayoutInflater();
		titleText.setText(gradebookTitleResource);
		displayCourse();
		loadAndDisplayUserGradebookItems();
	}

	private void displayCourse() {
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
	}
	
	private void loadAndDisplayUserGradebookItems() {
		if (gradebookAdapter == null) {
			gradebookAdapter = new UserGradebookItemAdapter(this);
			gradebookAdapter.beginLoading();
			buildService(new FetchUserGradebookItemsForCourseId(course.getId(), true)).execute();
		}
		setListAdapter(gradebookAdapter);
	}

    public void onServiceCallSuccess(FetchUserGradebookItemsForCourseId service) {
    	gradebookAdapter.updateItems(service.getResult());
    }
    public void onServiceCallException(FetchUserGradebookItemsForCourseId service, Exception ex) {
    	gradebookAdapter.hasError();
    }
    
	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		UberItem<UserGradebookItem> item = (UberItem<UserGradebookItem>)l.getItemAtPosition(position);
		
		if (item.getDataItem().getGrade() != null) {
			GradebookItem gradebookItem = item.getDataItem().getGradebookItem();
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
	
	protected class UserGradebookItemAdapter extends UberAdapter<UserGradebookItem> {
		
		public UserGradebookItemAdapter (Context context) {
			super(context,false,false,false);
		}
		
		@Override
		protected View getDataItemView(View convertView, ViewGroup parent,
				UberItem<UserGradebookItem> item) {
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
			UserGradebookItem ugbi = item.getDataItem();
			GradebookItem gradebookItem = ugbi.getGradebookItem();
			Grade grade = ugbi.getGrade();
			holder.titleText.setText(gradebookItem.getTitle());
			if (null == grade) {
				holder.dateText.setText(noGradeResource);
				holder.gradeText.setText(null);
			} else {
				holder.dateText.setText(prettyTimeFormatter.format(grade.getUpdatedDate().getTime()));
				holder.gradeText.setText(ugbi.getDisplayedGrade());
			}
			return convertView;
		}
	}
}
