package com.ecollege.android.activities;

import roboguice.activity.RoboTabActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.ecollege.android.ECollegeApplication;

public class ECollegeTabActivity extends RoboTabActivity implements ECollegeActivity {
    
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	super.onCreateDialog(id);
        return ECollegeActivityHelper.createProgressDialog(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return ECollegeActivityHelper.onCreateOptionsMenu(this,menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return ECollegeActivityHelper.onOptionsItemSelected(this,item);
    }    
}
