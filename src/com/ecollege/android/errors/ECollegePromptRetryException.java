package com.ecollege.android.errors;

import com.ecollege.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

@SuppressWarnings("serial")
public class ECollegePromptRetryException extends ECollegeException {

	protected DialogInterface.OnClickListener retryHandler;
	
	public ECollegePromptRetryException(Context ctx, DialogInterface.OnClickListener retryHandler, int errorMessageId,
			Throwable source) {
		super(ctx, errorMessageId, source);
		this.retryHandler = retryHandler;
	}

	public ECollegePromptRetryException(Context ctx, DialogInterface.OnClickListener retryHandler, int errorMessageId) {
		super(ctx, errorMessageId);
		this.retryHandler = retryHandler;
	}

	public ECollegePromptRetryException(Context ctx, DialogInterface.OnClickListener retryHandler, Throwable source) {
		super(ctx, source);
		this.retryHandler = retryHandler;
	}

	public void showErrorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(R.string.error_dialog_default_title);
		builder.setMessage(errorMessageId);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		builder.setPositiveButton(R.string.retry_dialog_button, retryHandler);
		builder.create().show();
	}

	
}
