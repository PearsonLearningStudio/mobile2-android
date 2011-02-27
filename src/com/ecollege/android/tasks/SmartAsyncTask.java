package com.ecollege.android.tasks;

//Based on example from droid-fu

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
public abstract class SmartAsyncTask<ParameterT, ProgressT, ReturnT> extends
        AsyncTask<ParameterT, ProgressT, ReturnT> {

    private final ECollegeApplication appContext;
    private boolean isECollegeActivity;

    private Exception error;

    private final String callerId;

    private SmartAsyncTaskCallable<ParameterT, ProgressT, ReturnT> callable;

    private boolean reportsProgress = false;
    private boolean isModalDialog = false;
    private int progressDialogTitleId = -1;
    private int progressDialogMsgId = -1;
    
    /**
     * Creates a new SmartAsyncTask who displays a progress dialog on the specified Context.
     *
     * @param context
     */
    public SmartAsyncTask(Context context) {

        if (!(context.getApplicationContext() instanceof ECollegeApplication)) {
            throw new IllegalArgumentException(
                    "must be used as part of ECollegeApplication");
        }
        this.appContext = (ECollegeApplication) context.getApplicationContext();
        this.callerId = context.getClass().getCanonicalName();
        this.isECollegeActivity = context instanceof ECollegeActivity;
        appContext.setActiveContext(callerId, context);
    }
    
    public SmartAsyncTask<ParameterT, ProgressT, ReturnT> makeModal() {
    	this.isModalDialog = true;
    	return this;
    }
    
    public SmartAsyncTask<ParameterT, ProgressT, ReturnT> enableProgressHandling() {
    	this.reportsProgress = true;
    	return this;
    }

	public SmartAsyncTask<ParameterT, ProgressT, ReturnT> setProgressDialogTitleId(int progressDialogTitleId) {
		this.progressDialogTitleId=progressDialogTitleId;
		return this;
	}

	public SmartAsyncTask<ParameterT, ProgressT, ReturnT> setProgressDialogMsgId(int progressDialogMsgId) {
		this.progressDialogMsgId=progressDialogMsgId;
		return this;
	}
    
    
    /**
     * Gets the most recent instance of this Context.
     * This may not be the Context used to construct this SmartAsyncTask as that Context might have been destroyed
     * when a incoming call was received, or the user rotated the screen.
     *
     * @return The current Context, or null if the current Context has ended, and a new one has not spawned.
     */
    protected Context getCallingContext() {
        try {
            Context caller = (Context) appContext.getActiveContext(callerId);
            if (caller == null || !this.callerId.equals(caller.getClass().getCanonicalName())
                    || (caller instanceof Activity && ((Activity) caller).isFinishing())) {
                // the context that started this task has died and/or was
                // replaced with a different one
                return null;
            }
            return caller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected final void onPreExecute() {
        Context context = getCallingContext();
        if (context == null) {
            Log.d(SmartAsyncTask.class.getSimpleName(), "skipping pre-exec handler for task "
                    + hashCode() + " (context is null)");
            cancel(true);
            return;
        }

        if (isECollegeActivity) {
            Activity activity = (Activity) context;
            ECollegeActivity eactivity = (ECollegeActivity)context;
            
            if (isModalDialog) {
            	eactivity.getApp().setNextProgressDialogTitleId(progressDialogTitleId);
            	eactivity.getApp().setNextProgressDialogMsgId(progressDialogMsgId);
            	activity.showDialog(0);
            } else {
            	if (reportsProgress) {
                	activity.setProgressBarVisibility(true);
            	} else {
                    activity.setProgressBarIndeterminateVisibility(true);
            	}
            }
        }
        before(context);
    }

    /**
     * Override to run code in the UI thread before this Task is run.
     *
     * @param context
     */
    protected void before(Context context) {
    }

    @Override
    protected final ReturnT doInBackground(ParameterT... params) {
        ReturnT result = null;
        Context context = getCallingContext();
        try {
            result = doCheckedInBackground(context, params);
        } catch (Exception e) {
            this.error = e;
        }
        return result;
    }

    /**
     * Override to perform computation in a background thread
     *
     * @param context
     * @param params
     * @return
     * @throws Exception
     */
    protected ReturnT doCheckedInBackground(Context context, ParameterT... params) throws Exception {
        if (callable != null) {
            return callable.call(this);
        }
        return null;
    }

    /**
     * Runs in the UI thread if there was an exception throw from doCheckedInBackground
     *
     * @param context The most recent instance of the Context that executed this SmartAsyncTask
     * @param error The thrown exception.
     */
    protected abstract void handleError(Context context, Exception error);

    @Override
    protected final void onPostExecute(ReturnT result) {
        Context context = getCallingContext();
        if (context == null) {
            Log.d(SmartAsyncTask.class.getSimpleName(), "skipping post-exec handler for task "
                    + hashCode() + " (context is null)");
            return;
        }


        if (isECollegeActivity) {
            Activity activity = (Activity) context;
            if (isModalDialog) {
                activity.removeDialog(0);
            } else {
            	if (reportsProgress) {
                	activity.setProgressBarVisibility(false);
            	} else {
                    activity.setProgressBarIndeterminateVisibility(false);
            	}
            }
        }

        if (failed()) {
            handleError(context, error);
        } else {
            after(context, result);
        }
    }

    /**
     * A replacement for onPostExecute. Runs in the UI thread after doCheckedInBackground returns.
     *
     * @param context The most recent instance of the Context that executed this SmartAsyncTask
     * @param result The result returned from doCheckedInBackground
     */
    protected abstract void after(Context context, ReturnT result);

    /**
     * Has an exception been thrown inside doCheckedInBackground()
     * @return
     */
    public boolean failed() {
        return error != null;
    }

    /**
     * Use a SmartAsyncTaskCallable instead of overriding doCheckedInBackground()
     *
     * @param callable
     */
    public void setCallable(SmartAsyncTaskCallable<ParameterT, ProgressT, ReturnT> callable) {
        this.callable = callable;
    }

}
