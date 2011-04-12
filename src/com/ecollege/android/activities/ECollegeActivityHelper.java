package com.ecollege.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.HomeActivity;
import com.ecollege.android.LoginActivity;
import com.ecollege.android.MainActivity;
import com.ecollege.android.R;


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
    

    public static void onCreate(Activity activity, Bundle savedInstanceState) {
        // Request progress bar
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

		ECollegeApplication app = (ECollegeApplication)activity.getApplication();
		app.setActiveContext(activity.getClass().getCanonicalName(), activity);
    }
    
	public static boolean onCreateOptionsMenu(Activity activity, Menu menu) {
		if (activity instanceof LoginActivity) {
			//do nothing for login activity
			return false;
		} else {
			activity.getMenuInflater().inflate(R.menu.default_menu, menu);
			
			if (activity instanceof HomeActivity) {
				MenuItem homeItem = (MenuItem)menu.findItem(R.id.home_menu_item);	
				homeItem.setVisible(false);
			}
			
			return true;
		}
	}
	
	public static boolean onOptionsItemSelected(Activity activity, MenuItem item) {
		if (item.getItemId() == R.id.logout_menu_item) {
			ECollegeApplication app = (ECollegeApplication)activity.getApplication();
			app.logout();
			return true;
		}		
		if (item.getItemId() == R.id.home_menu_item) {
			Intent i = new Intent(activity,MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(i);
			return true;
		}
		return false;
	}
	
}
