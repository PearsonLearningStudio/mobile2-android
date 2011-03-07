package com.ecollege.android.errors;

import com.ecollege.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

@SuppressWarnings("serial")
public class ECollegePromptException extends ECollegeException {

	public ECollegePromptException(Context ctx, int errorMessageId,
			Throwable source) {
		super(ctx, errorMessageId, source);
	}

	public ECollegePromptException(Context ctx, int errorMessageId) {
		super(ctx, errorMessageId);
	}

	public ECollegePromptException(Context ctx, Throwable source) {
		super(ctx, source);
	}
	
	public void showErrorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(R.string.error_dialog_default_title);
		builder.setMessage(errorMessageId);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		
		builder.create().show();
	}

}
