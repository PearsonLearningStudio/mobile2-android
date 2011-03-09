package com.ecollege.android.tasks;

import com.ecollege.api.services.BaseService;

public abstract class TaskPostProcessor<ServiceT extends BaseService> {
	
	public abstract ServiceT onPostProcess(ServiceT service); 
	
}
