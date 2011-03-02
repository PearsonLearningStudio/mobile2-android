package com.ecollege.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.R;

public class HeaderView extends FrameLayout {
    private final ProgressBar progressBar;

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
        
//        subtitleText = (TextView) findViewById(R.id.subtitle_text);
//        subtitleIcon = (ImageView) findViewById(R.id.subtitle_icon);
        progressBar = (ProgressBar) findViewById(R.id.loading_indicator);
//        secondaryLayout = findViewById(R.id.header_secondary);
	}
	
	public void setProgressVisibility(boolean visible) {
		if (progressBar != null) progressBar.setVisibility(visible ? VISIBLE : INVISIBLE);
	}
	
}
