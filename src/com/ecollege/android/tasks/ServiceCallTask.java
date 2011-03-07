package com.ecollege.android.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import roboguice.util.Ln;

import android.content.Context;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.api.services.BaseService;

public class ServiceCallTask<ServiceT extends BaseService> extends ECollegeAsyncTask<ServiceT> {
	
	private ServiceT service;
	
	public ServiceCallTask(ECollegeApplication app, ServiceT service)
	{
		super(app);
		this.service=service;
	}
	
	public ServiceT call() throws Exception {
		app.getClient().executeService(service);
		return service;
	}
	
	@Override
	protected void onException(Exception sourceException) throws RuntimeException {
		boolean errorHandled = false;
		
		String resultExceptionMethod = "onServiceCallException";
		
		try {
			Context c = currentContext.get();
			Method exceptionHandler = c.getClass().getMethod(resultExceptionMethod, service.getClass(), Exception.class);
			errorHandled = (Boolean)exceptionHandler.invoke(c,service,sourceException);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			Ln.i("onException","No exception handler found in " + service.getClass().getSimpleName());
			//no success handler found
		} catch (IllegalArgumentException e) {
			//problem calling method with arg
			app.reportError(e);
		} catch (IllegalAccessException e) {
			//problem calling method with permissions
			app.reportError(e);
		} catch (InvocationTargetException e) {
			app.reportError(e.getTargetException());
		}	
		
		if (!errorHandled) {
			super.onException(sourceException);
		}
		
	}

	@Override
	protected void onSuccess(ServiceT t) throws Exception {
		// TODO Auto-generated method stub
		super.onSuccess(t);
		
		
		String resultSuccessMethod = "onServiceCallSuccess";
		
		try {
			Context c = currentContext.get();
			Method successHandler = c.getClass().getMethod(resultSuccessMethod, service.getClass());
			successHandler.invoke(c,t);
		} catch (NoSuchMethodException e) {
			//no success handler found
		} catch (SecurityException e) {
			app.reportError(e);
		} catch (IllegalArgumentException e) {
			//problem calling method with arg
			app.reportError(e);
		} catch (IllegalAccessException e) {
			//problem calling method with permissions
			app.reportError(e);
		} catch (InvocationTargetException e) {
			app.reportError(e.getTargetException());
		}		
	}
	
//	public ServiceCallTask(Context context) {
//		super(context);
//		// TODO Auto-generated constructor stub
//	}
//	
//	private ECollegeActivity getActivity(Context context) {
//		return (ECollegeActivity) context;
//	}
//	
//	@Override
//	protected void handleError(Context context, Exception error) {
////		getActivity(context).hasRunningTask(false);
////		getActivity(context).reportError((RBException) error);
//	}
//
//	@Override
//	protected ServiceT doCheckedInBackground(Context context,
//			ServiceT... params) throws Exception {
//		// TODO Auto-generated method stub
//		
//
//		ServiceT service = (ServiceT)params[0];
//		ECollegeClient client = getActivity(context).getApp().getClient();
//		client.executeService(service);
//		return service;
//	}
//
//	@Override
//	protected void after(Context context, ServiceT result) {
//		// Show a progress indicator text in the application title bar
//		
//		String resultClassName = result.getClass().getSimpleName();
//		String resultSuccessMethod = "on" + resultClassName + "Success";
//		
//		try {
//			Method successHandler = context.getClass().getMethod(resultSuccessMethod, result.getClass());
//			successHandler.invoke(context,result);
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			//no success handler found
//		} catch (IllegalArgumentException e) {
//			//problem calling method with arg
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			//problem calling method with permissions
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		
//		
////		getActivity(context).setTitle(context.getText(R.string.app_name).toString());
////		getActivity(context).hasRunningTask(false);
////		afterTask(getActivity(context), result);
//	}

}
