package com.ecollege.android.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecollege.android.R;

public class HeaderView extends FrameLayout {
    private final ProgressBar busyIndicator;
    private final TextView headerTitle;

	public HeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
				
        LayoutInflater.from(context).inflate(R.layout.header_view, this, true);
        
        headerTitle = (TextView) findViewById(R.id.header_title);
        busyIndicator = (ProgressBar) findViewById(R.id.busy_indicator);
        
        if (context instanceof Activity) {
        	headerTitle.setText(((Activity)context).getTitle());	
        }
	}
	
	public void setProgressVisibility(boolean visible) {
		if (busyIndicator != null) busyIndicator.setVisibility(visible ? VISIBLE : INVISIBLE);
	}
	
}
