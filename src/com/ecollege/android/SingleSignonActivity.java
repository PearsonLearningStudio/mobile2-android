package com.ecollege.android;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class SingleSignonActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.webview) WebView webView;
    @InjectView(R.id.busy_indicator) ProgressBar busyIndicator;
    
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.string.sso_url) String sso_url;
	
	protected ECollegeClient client;
	protected String redirectUrl;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        client = app.getClient();
        setContentView(R.layout.singlesignon);
        
        redirectUrl = "http://localhost/catch_this_url.html";
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (checkUrl(url)) return true;
        		return super.shouldOverrideUrlLoading(view, url);
        	}
        	
        	@Override
        	public void onPageStarted(WebView view, String url, Bitmap favicon) {
        		if (checkUrl(url)) {
        			return;
        		}
        		busyIndicator.setVisibility(View.VISIBLE);
        		super.onPageStarted(view, url, favicon);
        	}
        	
        	@Override
        	public void onPageFinished(WebView view, String url) {
        		busyIndicator.setVisibility(View.INVISIBLE);
        		super.onPageFinished(view, url);
        	}
        });
        
//        webView.setWebChromeClient(new WebChromeClient() {
//        	@Override
//        	public void onProgressChanged(WebView view, int newProgress) {
//        		webProgress.setProgress(newProgress * 100);
//        	}
//        });
        webView.loadUrl(sso_url + "?redirect_url=" + redirectUrl);
        //webView.buildDrawingCache(false);
        //webView.setBackgroundColor(Color.TRANSPARENT);
        //webView.invalidate();
    }
    
    protected boolean checkUrl(String url) {
		if (url.startsWith(redirectUrl)) {
			UrlQuerySanitizer sanitzer = new UrlQuerySanitizer(url);
			String grantToken = sanitzer.getValue("grant_token");

			client.setupAuthentication(grantToken);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("grantToken",grantToken);
			editor.commit(); //change to apply if android 2.2
			
			webView.stopLoading();
			
			setResult(RESULT_OK);
			finish();
			
			return true;
		}
		return false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
    		webView.goBack();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
}