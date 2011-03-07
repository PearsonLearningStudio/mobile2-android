package com.ecollege.android.errors;

import com.ecollege.android.R;

import android.content.Context;

@SuppressWarnings("serial")
public abstract class ECollegeException extends RuntimeException {

	protected Context ctx;
	protected Throwable source;
	protected int errorMessageId;
	
	public ECollegeException(Context ctx, int errorMessageId) {
		this.ctx = ctx;
		this.errorMessageId = errorMessageId;
	}

	public ECollegeException(Context ctx, int errorMessageId, Throwable source) {
		this.ctx = ctx;
		this.errorMessageId = errorMessageId;
		this.source = source;
	}
	
	public ECollegeException(Context ctx, Throwable source) {
		this.source = source;
		this.errorMessageId = R.string.e_unhandled_exception;
		this.ctx = ctx;
	}

	public int getErrorMessageId() {
		return errorMessageId;
	}
	
	public Throwable getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		return ctx.getString(errorMessageId);
	}
	
}
