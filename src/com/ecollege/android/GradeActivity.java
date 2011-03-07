package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.Grade;
import com.ecollege.api.model.GradebookItem;
import com.ecollege.api.services.grades.FetchGradebookItemByGuid;
import com.ecollege.api.services.grades.FetchMyGradebookItemGrade;
import com.google.inject.Inject;

public class GradeActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra("courseId") long courseId;
	@InjectExtra("gradebookItemGuid") String gradebookItemGuid;
	@InjectView(R.id.main_text) TextView mainText;
	protected ECollegeClient client;
	protected GradebookItem gradebookItem;
	protected Grade grade;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade);
        client = app.getClient();
        fetchData();
    }
    
    protected void fetchData() {
    	app.buildService(new FetchGradebookItemByGuid(courseId,gradebookItemGuid)).execute();
    	app.buildService(new FetchMyGradebookItemGrade(courseId,gradebookItemGuid)).execute();
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
    	StringBuilder content = new StringBuilder();
    	
    	if (gradebookItem != null){
    		content.append("Item: " + gradebookItem.getTitle() + " (" + gradebookItem.getType() + ")\n");
    	}
    	
    	if (grade != null) {
    		content.append("Grade: " + grade.getLetterGrade() + "\n");
    		content.append("Points: " + grade.getPoints());
    		if (gradebookItem != null) content.append(" / " + gradebookItem.getPointsPossible());
    		content.append("\n");
    		content.append("Comments: " + grade.getComments());
    	}
    	
    	mainText.setText(content);    	
    }
}