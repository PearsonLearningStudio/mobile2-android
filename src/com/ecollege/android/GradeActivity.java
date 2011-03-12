package com.ecollege.android;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.apache.commons.lang.math.NumberUtils;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Grade;
import com.ecollege.api.model.GradebookItem;
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
	@InjectView(R.id.letter_grade_text) TextView letterGradeText;
	@InjectView(R.id.date_text) TextView dateText;
	@InjectView(R.id.view_all_button) Button viewAllButton;
	
	protected ECollegeClient client;
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
    
    protected void updateText() {
    	if (gradebookItem != null){
    		gradeTitleText.setText(gradebookItem.getTitle()); 	
    	}
    	
    	if (grade != null) {
    		commentsText.setText(grade.getComments());
    		letterGradeText.setText(grade.getLetterGrade());
    		dateText.setText(prettyTimeFormatter.format(grade.getUpdatedDate().getTime()));
    		
    		StringBuilder pointsContent = new StringBuilder();
    		pointsContent.append(decimalFormatter.format(grade.getPoints()));
    		if (gradebookItem != null) pointsContent.append(" / " + decimalFormatter.format(gradebookItem.getPointsPossible()));
    		pointsText.setText(pointsContent.toString());
    	}
    	
    }
}