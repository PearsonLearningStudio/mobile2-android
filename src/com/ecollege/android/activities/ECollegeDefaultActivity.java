package com.ecollege.android.activities;

import roboguice.activity.RoboActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import com.ecollege.android.ECollegeApplication;

public class ECollegeDefaultActivity extends RoboActivity implements ECollegeActivity {
	
	public ECollegeApplication getApp() {		
        return (ECollegeApplication)getApplication();
	}	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	super.onCreateDialog(id);
        return ECollegeActivityHelper.createProgressDialog(this);
    }
    
    
	
}
