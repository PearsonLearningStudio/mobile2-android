package com.ecollege.android;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.apache.commons.lang.math.NumberUtils;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Strings;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.Grade;
import com.ecollege.api.model.GradebookItem;
import com.ecollege.api.services.courses.FetchCourseByIdService;
import com.ecollege.api.services.grades.FetchGradebookItemByGuid;
import com.ecollege.api.services.grades.FetchMyGradebookItemGrade;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class GradeActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra("courseId") long courseId;
	@InjectExtra("gradebookItemGuid") String gradebookItemGuid;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.grade_title_text) TextView gradeTitleText;
	@InjectView(R.id.comments_text) TextView commentsText;
	@InjectView(R.id.points_text) TextView pointsText;
	@InjectView(R.id.points_label) TextView pointsLabel;
	@InjectView(R.id.letter_grade_text) TextView letterGradeText;
	@InjectView(R.id.letter_grade_label) TextView letterGradeLabel;
	@InjectView(R.id.date_text) TextView dateText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	@InjectResource(R.string.none) String none;
	
	protected ECollegeClient client;
	protected Course course;
	protected GradebookItem gradebookItem;
	protected Grade grade;
	
	private static PrettyTime prettyTimeFormatter = new PrettyTime();
	private static DecimalFormat decimalFormatter = new DecimalFormat();
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade);
        client = app.getClient();
        fetchData();
    }
    
    protected void fetchData() {
    	buildService(new FetchCourseByIdService(courseId)).execute();
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
    
    public void onServiceCallSuccess(FetchCourseByIdService service) {
    	 course = service.getResult();
    	 updateText();
    }
    
    protected void updateText() {
    	if (course != null) {
    		courseTitleText.setText(course.getTitle());
    	}
    	
    	if (gradebookItem != null){
    		gradeTitleText.setText(gradebookItem.getTitle()); 	
    	}
    	
    	if (grade != null) {
    		if (Strings.notEmpty(grade.getComments())) {
    			commentsText.setText(grade.getComments());
    		} else {
    			commentsText.setText(none);
    		}
    		
    		// Assuming letter grades and points are exclusive or something
    		if (Strings.notEmpty(grade.getLetterGrade())) {
    			letterGradeText.setText(grade.getLetterGrade());
    		} else {
    			letterGradeText.setVisibility(View.GONE);
    			letterGradeLabel.setVisibility(View.GONE);
    		}
    		
    		if (gradebookItem == null || gradebookItem.getPointsPossible().floatValue() == 0) {
    			pointsLabel.setVisibility(View.GONE);
    			pointsText.setVisibility(View.GONE);
    		} else {
    			if (grade.getPoints() != null) {
		    		StringBuilder pointsContent = new StringBuilder();
		    		pointsContent.append(decimalFormatter.format(grade.getPoints()));
		    		if (gradebookItem != null) pointsContent.append(" / " + decimalFormatter.format(gradebookItem.getPointsPossible()));
		    		pointsText.setText(pointsContent.toString());
    			}
    		}
    		
    		dateText.setText(prettyTimeFormatter.format(grade.getUpdatedDate().getTime()));
    	}
    	
    }
}