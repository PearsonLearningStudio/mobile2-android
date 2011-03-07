package com.ecollege.android;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeDefaultActivity;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.DropboxMessage;
import com.ecollege.api.services.dropbox.FetchDropboxMessage;
import com.google.inject.Inject;

public class DropboxMessageActivity extends ECollegeDefaultActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectExtra("courseId") long courseId;
	@InjectExtra("basketId") long basketId;
	@InjectExtra("messageId") long messageId;
	@InjectView(R.id.main_text) TextView mainText;
	
	protected ECollegeClient client;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_message);
        client = app.getClient();
        fetchData();
    }
    
    protected void fetchData() {
    	app.buildService(new FetchDropboxMessage(courseId,basketId,messageId)).execute();
    }
    
    public void onServiceCallSuccess(FetchDropboxMessage service) {
    	DropboxMessage message = service.getResult(); 
    	
    	StringBuilder content = new StringBuilder();
    	
    	for (int i=0;i<message.getAttachments().size();i++) {
    		content.append("Attachment[" + i + "]: " + message.getAttachments().get(i).getName() + "\n");
    	}
    	
    	content.append("Comments: " + message.getComments());
    	
    	mainText.setText(content);    	
    }
}