package com.ecollege.android.activities;

import roboguice.activity.RoboListActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import com.ecollege.android.ECollegeApplication;

public class ECollegeListActivity extends RoboListActivity implements ECollegeActivity {
    
	private int progressDialogTitleId;
    private int progressDialogMsgId;
    
	public int getProgressDialogTitleId() {
		return progressDialogTitleId;
	}

	public void setProgressDialogTitleId(int progressDialogTitleId) {
		this.progressDialogTitleId = progressDialogTitleId;
	}

	public int getProgressDialogMsgId() {
		return progressDialogMsgId;
	}

	public void setProgressDialogMsgId(int progressDialogMsgId) {
		this.progressDialogMsgId = progressDialogMsgId;
	}
	
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
