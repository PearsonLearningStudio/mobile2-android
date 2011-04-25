package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.Course;
import com.ecollege.api.services.multimedia.FetchHtmlByIdService;
import com.google.inject.Inject;

public class HtmlContentActivity extends ECollegeDefaultActivity {
	
	public static final String HTML_ID_EXTRA = "HTML_ID_EXTRA";
	public static final String COURSE_EXTRA = "COURSE_EXTRA";
	public static final String TITLE_EXTRA = "TITLE_EXTRA";
	public static final String SCHEDULE_EXTRA = "SCHEDULE_EXTRA";
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	
	@InjectView(R.id.title_text) TextView titleText;
	@InjectView(R.id.course_title_text) TextView courseTitleText;
	@InjectView(R.id.webview) WebView webView;
	@InjectView(R.id.schedule_text) TextView scheduleText;
	
	@InjectExtra(COURSE_EXTRA) Course course;
	@InjectExtra(HTML_ID_EXTRA) long htmlId;
	@InjectExtra(TITLE_EXTRA) String title;
	@InjectExtra(SCHEDULE_EXTRA) String scheduleInfo;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_content);
		
		titleText.setText(title);
		courseTitleText.setText(Html.fromHtml(course.getTitle()));
		scheduleText.setText(scheduleInfo);
		webView.getSettings().setBuiltInZoomControls(true);
		
		upcomeHtmlContent(false);
	}
    
	private void upcomeHtmlContent(boolean reload) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		buildService(new FetchHtmlByIdService(course.getId(), htmlId))
			.configureCaching(cacheConfiguration)
			.execute();
	}

    public void onServiceCallSuccess(FetchHtmlByIdService service) {
    	webView.loadData(service.getResult(), "text/html", "utf-8");
    	//htmlContentText.setText(Html.fromHtml(service.getResult()),BufferType.SPANNABLE);
    }
}
