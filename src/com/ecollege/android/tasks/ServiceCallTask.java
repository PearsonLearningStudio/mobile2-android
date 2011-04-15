package com.ecollege.android.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import roboguice.util.Ln;
import android.content.Context;
import android.content.DialogInterface;

import com.ecollege.android.R;
import com.ecollege.android.activities.ECollegeActivity;
import com.ecollege.android.errors.ECollegePromptRetryException;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.exceptions.TimeoutException;
import com.ecollege.api.services.BaseService;

public class ServiceCallTask<ServiceT extends BaseService> extends ECollegeAsyncTask<ServiceT> {
	
	private static boolean VOLATILE_CACHE_ENABLED = false;
	
	protected ServiceT service;
	protected boolean useFileCache = true;
	protected boolean cacheExecutedResult = true;
	protected boolean useResultCache = true;
	protected boolean cacheInFileCache = true;
	protected TaskPostProcessor<ServiceT> postProcessor;
	
	public ServiceCallTask(ECollegeActivity activity, ServiceT service) {
		super(activity);
		this.service=service;
	}
	
	public ServiceCallTask<ServiceT> bypassFileCache() {
		useFileCache = false;
		return this;
	}
	
	public ServiceCallTask<ServiceT> doNotResultCache() {
		cacheExecutedResult = false;
		return this;
	}
	
	public ServiceCallTask<ServiceT> bypassResultCache() {
		useResultCache = false;
		return this;
	}
	
	public ServiceCallTask<ServiceT> doNotFileCache() {
		cacheInFileCache = false;
		return this;
	}
	
	public ServiceCallTask<ServiceT> configureCaching(CacheConfiguration config) {
		assert(config != null);
		cacheInFileCache = config.cacheResultInFileCache;
		useFileCache = !config.bypassFileCache;
		cacheExecutedResult = config.cacheResultInResultCache;
		useResultCache = !config.bypassResultCache;
		return this;
	}


	public ServiceCallTask<ServiceT> setPostProcessor(TaskPostProcessor<ServiceT> postProcessor) {
		this.postProcessor = postProcessor;
		return this;
	}
	
	public ServiceT call() throws Exception {
		if (useResultCache && VOLATILE_CACHE_ENABLED) {
			ServiceT executedService = getServiceFromResultCache(service);
			// We found an identical service that was already executed and cached,
			// so just return that one
			if (executedService != null) {
				Ln.i(String.format("Returning cached result: %s for %s instead of performing service call", executedService.toString(), service.toString()));
				service = executedService;
				return executedService;
			}
		} else {
			Ln.i(String.format("Bypassing result cache for %s", service));
		}
		
		app.getClient().executeService(service, app.getServiceCache(), useFileCache, cacheInFileCache);
		
		if (postProcessor != null) {
			service = postProcessor.onPostProcess(service);
		}
		
		if (cacheExecutedResult && service.isCacheable() && VOLATILE_CACHE_ENABLED) {
			app.putObjectInVolatileCache(
				service.getCacheKey(app.getSessionIdentifier()),
				service);
		}
		
		return service;
	}
	
	
	@SuppressWarnings("unchecked")
	protected ServiceT getServiceFromResultCache(ServiceT newService) {
		ServiceT completedService = app.getObjectOfTypeFromVolatileCache(
				newService.getCacheKey(app.getSessionIdentifier()),
				(Class<ServiceT>)newService.getClass());
		return completedService;
	}

	@Override
	protected void onException(Exception sourceException) throws RuntimeException {
		boolean errorHandled = false;
		String resultExceptionMethod = "onServiceCallException";
		ECollegeActivity currentActivity = getCurrentActivity();
		
		try {
			if (currentActivity != null) {
				Method exceptionHandler = currentActivity.getClass().getMethod(resultExceptionMethod, service.getClass(), Exception.class);
				errorHandled = (Boolean)exceptionHandler.invoke(currentActivity,service,sourceException);
			} else {
				Ln.e("ERROR! No current activity to attach exception to!");
			}
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
			if (sourceException instanceof TimeoutException) {
				DialogInterface.OnClickListener retryHandler = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						ServiceCallTask.this.execute();
					}
				};
				
				if (currentActivity != null) {
					ECollegePromptRetryException retryE = new ECollegePromptRetryException((Context)currentActivity, retryHandler, R.string.e_network_timeout);
					app.reportError(retryE);
				}
			} else {
				super.onException(sourceException);	
			}
		}
		
	}

	@Override
	protected void onSuccess(ServiceT t) throws Exception {
		// TODO Auto-generated method stub
		super.onSuccess(t);

		ECollegeActivity currentActivity = getCurrentActivity();
		
		String resultSuccessMethod = "onServiceCallSuccess";
		
		try {
			if (currentActivity != null) {
				Method successHandler = currentActivity.getClass().getMethod(resultSuccessMethod, service.getClass());
				successHandler.invoke(currentActivity,t);
			} else {
				Ln.e("ERROR! No current activity to attach success to!");
			}
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
