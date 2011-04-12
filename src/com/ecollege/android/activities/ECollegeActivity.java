package com.ecollege.android.activities;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.services.BaseService;

public interface ECollegeActivity {
	public static final int LOGIN_REQUEST_CODE = 1001;
	public static final int SSO_LOGIN_REQUEST_CODE = 1002;
	public static final int MAIN_ACTIVITY_REQUEST_CODE = 1003;
	
	public ECollegeApplication getApp();

	public <ServiceT extends BaseService> ServiceCallTask<ServiceT> buildService(ServiceT service);
	
}
