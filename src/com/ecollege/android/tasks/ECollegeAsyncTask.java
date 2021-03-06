package com.ecollege.android.tasks;

//Based on example from droid-fu

import roboguice.util.RoboAsyncTask;
import android.app.Activity;
import android.content.Context;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.activities.ECollegeActivity;

/**
 * Works in a similar way to AsyncTask but provides extra functionality.
 *
 * 1) It keeps track of the active instance of each Context, ensuring that the
 * correct instance is reported to. This is very useful if your Activity is
 * forced into the background, or the user rotates his device.
 *
 * 2) A progress dialog is automatically shown. See useCustomDialog()
 * disableDialog()
 *
 * 3) If an Exception is thrown from inside doInBackground, this is now handled
 * by the handleError method.
 *
 * 4) You should now longer override onPreExecute(), doInBackground() and
 * onPostExecute(), instead you should use before(), doCheckedInBackground() and
 * after() respectively.
 *
 * These features require that the Application extends DroidFuApplication.
 *
 * @param <ParameterT>
 * @param <ProgressT>
 * @param <ReturnT>
 */
public abstract class ECollegeAsyncTask<ResultT> extends RoboAsyncTask<ResultT>  {

	protected ECollegeApplication app;
    @SuppressWarnings("unused")
	private boolean reportsProgress = false;
    private int progressDialogTitleId = -1;
    private int progressDialogMsgId = -1;
    private boolean showModalDialog = false;
	private boolean showTitlebarBusyIndicator = true;
    private String activityName;
	
    public ECollegeAsyncTask(ECollegeActivity activity) {
    	super();
    	this.app=activity.getApp();
    	app.getInjector().injectMembers(this);
    	activityName = activity.getClass().getCanonicalName();
    	app.setActiveContext(activityName,(Context)activity);
    }
    
    @Override
	protected void onException(Exception e) throws RuntimeException {
		app.reportError(e);
	}

	public ECollegeAsyncTask<ResultT> makeModal() {
    	this.showModalDialog = true;
		this.showTitlebarBusyIndicator = false;
    	return this;
    }
    
    public ECollegeAsyncTask<ResultT> enableProgressHandling() {
    	this.reportsProgress = true;
    	return this;
    }

	public ECollegeAsyncTask<ResultT> setProgressDialogTitleId(int progressDialogTitleId) {
		this.progressDialogTitleId=progressDialogTitleId;
		return this;
	}

	public ECollegeAsyncTask<ResultT> setProgressDialogMsgId(int progressDialogMsgId) {
		this.progressDialogMsgId=progressDialogMsgId;
		return this;
	}
	
	public ECollegeAsyncTask<ResultT> disableTitlebarBusyIndicator() {
		this.showTitlebarBusyIndicator = false;
		return this;
	}
   
	protected ECollegeActivity getCurrentActivity() {
		return (ECollegeActivity)app.getActiveContext(activityName);
	}
	
    @Override
    protected void onPreExecute() throws Exception {
    	super.onPreExecute();

    	if (showTitlebarBusyIndicator) app.incrementPendingServiceCalls();
    	
    	ECollegeActivity currentActivity = getCurrentActivity();
    	
        if (currentActivity != null && showModalDialog) {
        	app.setNextProgressDialogTitleId(progressDialogTitleId);
        	app.setNextProgressDialogMsgId(progressDialogMsgId);
        	((Activity)currentActivity).showDialog(0);
        }
    }


	@Override
	protected void onFinally() throws RuntimeException {
		super.onFinally();

		if (showTitlebarBusyIndicator) app.decrementPendingServiceCalls();

    	ECollegeActivity currentActivity = getCurrentActivity();
    	
        if (currentActivity != null && showModalDialog) {
            ((Activity)currentActivity).removeDialog(0);
        }
	}
}
