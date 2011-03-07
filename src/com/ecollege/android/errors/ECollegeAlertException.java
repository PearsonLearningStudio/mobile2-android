package com.ecollege.android.errors;

import android.content.Context;

@SuppressWarnings("serial")
public class ECollegeAlertException extends ECollegeException {

	public ECollegeAlertException(Context ctx, int errorMessageId,
			Throwable source) {
		super(ctx, errorMessageId, source);
	}

	public ECollegeAlertException(Context ctx, int errorMessageId) {
		super(ctx, errorMessageId);
	}

	public ECollegeAlertException(Context ctx, Throwable source) {
		super(ctx, source);
	}

}
