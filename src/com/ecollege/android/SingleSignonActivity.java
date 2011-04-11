package com.ecollege.android;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.google.inject.Inject;

public class SingleSignonActivity extends ECollegeDefaultActivity {
    @InjectView(R.id.webview) WebView webView;
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.string.sso_url) String sso_url;
	
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = app.getClient();
        setContentView(R.layout.singlesignon);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (url.contains("catch_this_url.html")) {
        			UrlQuerySanitizer sanitzer = new UrlQuerySanitizer(url);
        			String grantToken = sanitzer.getValue("grant_token");

        			client.setupAuthentication(grantToken);
        			
        			SharedPreferences.Editor editor = prefs.edit();
        			editor.putString("grantToken",grantToken);
        			editor.commit(); //change to apply if android 2.2
        			
        			view.stopLoading();
        			
        			setResult(RESULT_OK);
        			finish();
        			
        			return true;
        		}
        		return super.shouldOverrideUrlLoading(view, url);
        	}
        });
        webView.loadUrl(sso_url + "?redirect_url=http://localhost/catch_this_url.html");
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