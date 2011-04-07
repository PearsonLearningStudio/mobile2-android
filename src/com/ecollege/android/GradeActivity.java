package com.ecollege.android;

import java.text.DecimalFormat;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Strings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.util.DateTimeUtil;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.Grade;
import com.ecollege.api.model.GradebookItem;
import com.ecollege.api.services.grades.FetchGradebookItemByGuid;
import com.ecollege.api.services.grades.FetchMyGradebookItemGrade;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class GradeActivity extends ECollegeDefaultActivity {
	
	public static final String FINISH_ON_CLICK_ALL_GRADES_EXTRA = "FINISH_ON_CLICK_ALL_GRADES_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra("courseId") long courseId;
	@InjectExtra("gradebookItemGuid") String gradebookItemGuid;
	@InjectExtra(value = FINISH_ON_CLICK_ALL_GRADES_EXTRA, optional = true) boolean finishOnClickAllGrades;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.grade_title_text) TextView gradeTitleText;
	@InjectView(R.id.comments_text) TextView commentsText;
	@InjectView(R.id.grade_text) TextView gradeText;
	@InjectView(R.id.date_text) TextView dateText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	@InjectResource(R.string.no_comments) String no_comments;
	@InjectResource(R.string.grade_value) String grade_value;
	
	protected ECollegeClient client;
	protected Course course;
	protected GradebookItem gradebookItem;
	protected Grade grade;
	
	private static DecimalFormat decimalFormatter = new DecimalFormat();
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade);
        client = app.getClient();
    	course = app.getCourseById(courseId);
    	updateText();
    	
    	viewAllButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				viewAllCourseGrades();
			}
		});
    	
        fetchData();
    }
    
	protected void fetchData() {
    	buildService(new FetchGradebookItemByGuid(courseId,gradebookItemGuid)).execute();
    	buildService(new FetchMyGradebookItemGrade(courseId,gradebookItemGuid)).execute();
    }
    
    public void onServiceCallSuccess(FetchGradebookItemByGuid service) {
    	gradebookItem = service.getResult(); 
    	updateText();
    }

    public void onServiceCallSuccess(FetchMyGradebookItemGrade service) {
    	grade = service.getResult(); 
    	updateText();
    }
    
    protected void viewAllCourseGrades() {
    	if (finishOnClickAllGrades) {
    		finish();
    	} else {
    		Intent intent = new Intent(this, CourseGradebookActivity.class);
    		intent.putExtra(CoursesActivity.COURSE_EXTRA, course);
    		startActivity(intent);
    	}
	}

    protected void updateText() {
    	if (course != null) {
    		courseTitleText.setText(Html.fromHtml(course.getTitle()));
    	}
    	
    	if (gradebookItem != null){
    		gradeTitleText.setText(Html.fromHtml(gradebookItem.getTitle()));
    	}
    	
    	if (grade != null) {
    		if (Strings.notEmpty(grade.getComments())) {
    			commentsText.setText(Html.fromHtml(grade.getComments()));
    		} else {
    			commentsText.setText(Html.fromHtml("<i>" + no_comments + "</i>"));
    		}
    		
    		String gradeValue = "-";
    		if (Strings.notEmpty(grade.getLetterGrade())) {
    			gradeValue = grade.getLetterGrade();
    		}
    		
    		if (gradebookItem != null && gradebookItem.getPointsPossible() != null && gradebookItem.getPointsPossible().floatValue() != 0) {
    			if (grade.getPoints() != null) {
    				gradeValue = String.format("%s (%s/%s)",
    						gradeValue,
    						decimalFormatter.format(grade.getPoints()),
    						decimalFormatter.format(gradebookItem.getPointsPossible()));
    			}
    		}
    		
    		gradeText.setText(String.format(grade_value, gradeValue));
    		dateText.setText(DateTimeUtil.getLongFriendlyDate(grade.getUpdatedDate()));
    	}
    	
    }
}