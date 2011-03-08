package com.ecollege.android.activities;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.services.BaseService;

public interface ECollegeActivity {
	public static final int LOGIN_REQUEST_CODE = 1001;
	
	public ECollegeApplication getApp();

	public <ServiceT extends BaseService> ServiceCallTask<ServiceT> buildService(ServiceT service);
	
}
