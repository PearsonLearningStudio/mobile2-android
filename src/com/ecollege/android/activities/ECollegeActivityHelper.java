package com.ecollege.android.activities;

import com.ecollege.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;


public class ECollegeActivityHelper {

    public static ProgressDialog createProgressDialog(final ECollegeActivity eactivity) {
    	final Activity activity = (Activity)eactivity;
        ProgressDialog progressDialog = new ProgressDialog(activity);

        int progressDialogTitleId = eactivity.getApp().getNextProgressDialogTitleId();
        int progressDialogMsgId = eactivity.getApp().getNextProgressDialogMsgId();
        
        if (progressDialogTitleId <= 0) {
            progressDialogTitleId = R.string.progress_dialog_default_title;
        }
        if (progressDialogMsgId <= 0) {
            progressDialogMsgId = R.string.progress_dialog_default_message;
        }
        progressDialog.setTitle(progressDialogTitleId);
        progressDialog.setMessage(activity.getString(progressDialogMsgId));
        progressDialog.setIndeterminate(true);
        progressDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                activity.onKeyDown(keyCode, event);
                return false;
            }
        });
        // progressDialog.setInverseBackgroundForced(true);
        return progressDialog;
    }
	
}
