package com.ecollege.android.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;

import com.ecollege.android.activities.ECollegeActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.services.BaseService;

public class ServiceCallTask<ServiceT extends BaseService> extends SmartAsyncTask<ServiceT, Void, ServiceT> {

	public ServiceCallTask(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private ECollegeActivity getActivity(Context context) {
		return (ECollegeActivity) context;
	}
	
	@Override
	protected void handleError(Context context, Exception error) {
//		getActivity(context).hasRunningTask(false);
//		getActivity(context).reportError((RBException) error);
	}

	@Override
	protected ServiceT doCheckedInBackground(Context context,
			ServiceT... params) throws Exception {
		// TODO Auto-generated method stub
		

		ServiceT service = (ServiceT)params[0];
		ECollegeClient client = getActivity(context).getApp().getClient();
		client.executeService(service);
		return service;
	}

	@Override
	protected void after(Context context, ServiceT result) {
		// Show a progress indicator text in the application title bar
		
		String resultClassName = result.getClass().getSimpleName();
		String resultSuccessMethod = "on" + resultClassName + "Success";
		
		try {
			Method successHandler = context.getClass().getMethod(resultSuccessMethod, result.getClass());
			successHandler.invoke(context,result);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			//no success handler found
		} catch (IllegalArgumentException e) {
			//problem calling method with arg
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			//problem calling method with permissions
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		
//		getActivity(context).setTitle(context.getText(R.string.app_name).toString());
//		getActivity(context).hasRunningTask(false);
//		afterTask(getActivity(context), result);
	}

}
