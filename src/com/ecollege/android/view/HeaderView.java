package com.ecollege.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.R;

public class HeaderView extends FrameLayout {
    private final ProgressBar busyIndicator;

	public HeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
				
        LayoutInflater.from(context).inflate(R.layout.header_view, this, true);
        
        ECollegeApplication app = (ECollegeApplication)context.getApplicationContext();
        
        if (app != null) {
        	app.registerHeaderView(this);
        }
        
        busyIndicator = (ProgressBar) findViewById(R.id.busy_indicator);
        
	}
	
	public void setProgressVisibility(boolean visible) {
		if (busyIndicator != null) busyIndicator.setVisibility(visible ? VISIBLE : INVISIBLE);
	}
	
}
