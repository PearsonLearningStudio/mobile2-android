package com.ecollege.android.activities;

import com.ecollege.android.ECollegeApplication;
import com.github.droidfu.activities.BetterListActivity;

public class ECollegeListActivity extends BetterListActivity implements ECollegeActivity {

	public ECollegeApplication getApp() {		
        return (ECollegeApplication)getApplication();
	}
	
}
