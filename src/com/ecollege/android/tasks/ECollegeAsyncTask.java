package com.ecollege.android.tasks;

//Based on example from droid-fu

import roboguice.util.RoboAsyncTask;
import android.app.Activity;
import android.content.Context;

import com.ecollege.android.ECollegeApplication;
import com.google.inject.Inject;
import com.google.inject.Provider;

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

	@Inject protected Provider<Context> currentContext;

	protected ECollegeApplication app;
    @SuppressWarnings("unused")
	private boolean reportsProgress = false;
    private boolean isModalDialog = false;
    private int progressDialogTitleId = -1;
    private int progressDialogMsgId = -1;
    
    public ECollegeAsyncTask(ECollegeApplication app) {
    	super();
    	this.app=app;
    	app.getInjector().injectMembers(this);
    }
    
    public ECollegeAsyncTask<ResultT> makeModal() {
    	this.isModalDialog = true;
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
    
    @Override
    protected void onPreExecute() throws Exception {
    	super.onPreExecute();

        if (currentContext != null && currentContext.get() !=null) {
        	Context c = currentContext.get();
        	        	
            if (app != null && isModalDialog && c instanceof Activity) {
            	app.setNextProgressDialogTitleId(progressDialogTitleId);
            	app.setNextProgressDialogMsgId(progressDialogMsgId);
            	((Activity)c).showDialog(0);
            }
        }
    }


	@Override
	protected void onFinally() throws RuntimeException {
		super.onFinally();

        if (currentContext != null && currentContext.get() !=null) {
        	Context c = currentContext.get();
            if (isModalDialog && c instanceof Activity) {
            	((Activity)c).removeDialog(0);
            }
        }
	}
}
