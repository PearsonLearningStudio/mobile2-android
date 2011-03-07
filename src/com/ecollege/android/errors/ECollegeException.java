package com.ecollege.android.errors;

import com.ecollege.android.R;

import android.content.Context;

@SuppressWarnings("serial")
public class ECollegeException extends RuntimeException {

	private Context ctx;
	private Throwable source;
	private ECollegeErrorType errorType;
	private int errorMessageId;
	
	public ECollegeException(Context ctx, ECollegeErrorType errorType, int errorMessageId) {
		this.ctx = ctx;
		this.errorType = errorType;
		this.errorMessageId = errorMessageId;
	}

	public ECollegeException(Context ctx, ECollegeErrorType errorType, int errorMessageId, Throwable source) {
		this.ctx = ctx;
		this.errorType = errorType;
		this.errorMessageId = errorMessageId;
		this.source = source;
	}
	
	public ECollegeException(Context ctx, Throwable source) {
		this.source = source;
		this.errorType = ECollegeErrorType.PROMPT;
		this.errorMessageId = R.string.e_unhandled_exception;
		this.ctx = ctx;
	}

	public int getErrorMessageId() {
		return errorMessageId;
	}

	public ECollegeErrorType getErrorType() {
		return errorType;
	}
	
	public Throwable getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		return ctx.getString(errorMessageId);
	}
	
}
