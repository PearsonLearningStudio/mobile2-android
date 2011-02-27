package com.ecollege.android.activities;

import com.ecollege.android.ECollegeApplication;
import com.github.droidfu.activities.BetterDefaultActivity;

public class ECollegeDefaultActivity extends BetterDefaultActivity implements ECollegeActivity {

	public ECollegeApplication getApp() {		
        return (ECollegeApplication)getApplication();
	}
	
}
