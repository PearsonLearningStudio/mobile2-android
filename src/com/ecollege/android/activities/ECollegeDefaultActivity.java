package com.ecollege.android.activities;

import roboguice.activity.RoboActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.android.view.HeaderView;
import com.ecollege.api.services.BaseService;

public class ECollegeDefaultActivity extends RoboActivity implements ECollegeActivity {
	
	public ECollegeApplication getApp() {		
        return (ECollegeApplication)getApplication();
	}	

	public <ServiceT extends BaseService> ServiceCallTask<ServiceT> buildService(ServiceT service) {
		return new ServiceCallTask<ServiceT>(this, service);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ECollegeActivityHelper.onCreate(this,savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        ECollegeActivityHelper.onResume(this);
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
    
    @Override
    public void setContentView(int layoutResID) {
    	super.setContentView(layoutResID);
    	ECollegeActivityHelper.afterSetContentView(this,layoutResID);
    }

	public HeaderView getHeaderView() {
    	return ECollegeActivityHelper.getHeaderView(this);
	}

    
}
