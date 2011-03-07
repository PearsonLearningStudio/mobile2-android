package com.ecollege.android;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.LoadMoreAdapter;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.services.activities.FetchMyWhatsHappeningFeed;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class HomeActivity extends ECollegeListActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.radio_whats_due) RadioButton whatsDueRadioButton;
	@InjectView(R.id.radio_activity) RadioButton activityRadioButton;
	
	protected ECollegeClient client;
	private LayoutInflater mInflater;
	
	
	private static PrettyTime prettyTimeFormatter = new PrettyTime();
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	boolean showWhatsDue = prefs.getBoolean("showWhatsDue", true); 
    	whatsDueRadioButton.setChecked(showWhatsDue);
    	activityRadioButton.setChecked(!showWhatsDue);
        refreshList();

    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (whatsDueRadioButton != null) {
    		prefs.edit().putBoolean("showWhatsDue", whatsDueRadioButton.isChecked()).commit();
    	}
    }

    public void onRadioGroupCheckedChanged(View v) {
    	refreshList();
    }
    
    protected void refreshList() {
    	ListAdapter chosenAdapter;
    	if (whatsDueRadioButton.isChecked()) {
    		chosenAdapter = createOrReturnWhatsHappeningAdapter();
    	} else {
    		chosenAdapter = createOrReturnActivitiesAdapter();
    	}
    	setListAdapter(chosenAdapter);
    }

	private WhatsHappeningAdapter whatsHappeningAdapter;
    protected ListAdapter createOrReturnWhatsHappeningAdapter() {
    	if (whatsHappeningAdapter == null) {
    		whatsHappeningAdapter = new WhatsHappeningAdapter(this);
    	}
    	return whatsHappeningAdapter;
    }

	private LoadMoreAdapter activityAdapter;
	private boolean canLoadMoreActivites = true;
	
    private ListAdapter createOrReturnActivitiesAdapter() {
    	if (activityAdapter == null) {
			ActivityFeedAdapter baseAdapter = new ActivityFeedAdapter(this,new ArrayList<ActivityStreamItem>());
    		activityAdapter = new LoadMoreAdapter(this, baseAdapter, canLoadMoreActivites);
    		fetchWhatsHappening();
    	}
    	return activityAdapter;
    }
    
    protected void fetchWhatsHappening() {
		activityAdapter.setIsLoadingMore(true);
    	if (canLoadMoreActivites) {
    		GregorianCalendar oneMonthAgo = new GregorianCalendar();
    		oneMonthAgo.add(Calendar.MONTH, -2);
        	app.buildService(new FetchMyWhatsHappeningFeed(oneMonthAgo)).disableTitlebarProgress().execute();
    	} else {
    		app.buildService(new FetchMyWhatsHappeningFeed()).disableTitlebarProgress().execute();	
    	}
    }
    
    public void onServiceCallSuccess(FetchMyWhatsHappeningFeed service) {
		ActivityFeedAdapter baseAdapter = new ActivityFeedAdapter(this,service.getResult());
    	activityAdapter.update(baseAdapter,canLoadMoreActivites);
    	refreshList();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	if (id == LoadMoreAdapter.LOAD_MORE_ITEM_ID) {
    		canLoadMoreActivites = false;
    		fetchWhatsHappening();
    		return;
    	}
    	
    	ActivityStreamItem si = (ActivityStreamItem)getListAdapter().getItem(position);
        String objectType = si.getObject().getObjectType();
        
        if ("thread-topic".equals(objectType)) {
        	long topicId = Long.parseLong(si.getObject().getReferenceId());
        	Intent i = new Intent(this,DiscussionTopicActivity.class);
        	i.putExtra("topicId", topicId);
        	startActivity(i);
        } else if ("thread-post".equals(objectType)) {
        	long responseId = Long.parseLong(si.getObject().getReferenceId());
        	Intent i = new Intent(this,DiscussionResponseActivity.class);
        	i.putExtra("responseId", responseId);
        	startActivity(i);        	
        } else if ("grade".equals(objectType)) {
        	long courseId = si.getObject().getCourseId();
        	String gradebookItemGuid = (String)si.getTarget().getReferenceId();
        	Intent i = new Intent(this,GradeActivity.class);
        	i.putExtra("courseId", courseId);
        	i.putExtra("gradebookItemGuid", gradebookItemGuid);
        	startActivity(i);
        } else if ("dropbox-submission".equals(objectType)) {
        	long courseId = si.getObject().getCourseId();
        	long basketId = Long.parseLong(si.getTarget().getReferenceId().toString());
        	long messageId = Long.parseLong(si.getObject().getReferenceId());
        	Intent i = new Intent(this,DropboxMessageActivity.class);
        	i.putExtra("courseId", courseId);
        	i.putExtra("basketId", basketId);
        	i.putExtra("messageId", messageId);
        	startActivity(i);
        } else if ("exam-submission".equals(objectType)) {
        } else if ("remark".equals(objectType)) {
        } 
    	
    }
    
    static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timeText;
        ImageView icon;
    }
    
    private class WhatsHappeningAdapter extends ArrayAdapter<String> {    

    	public WhatsHappeningAdapter(Context c) {
    		super(c,R.layout.activity_item,new String[]{"placeholder"});
    	}
    	
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
                holder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
                //holder.iconPlaceholder = (TextView) convertView.findViewById(R.id.icon_stub);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            
            holder.titleText.setText("Pending");
            holder.descriptionText.setText("Waiting for API");
            //holder.iconPlaceholder.setText("!!");
            return convertView;
        }    	
    }
    
    private class ActivityFeedAdapter extends ArrayAdapter<ActivityStreamItem> {
    	
    	public ActivityFeedAdapter(Context c, List<ActivityStreamItem> streamItems) {
    		super(c,R.layout.activity_item,streamItems);
    	}

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
                holder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
                holder.timeText = (TextView) convertView.findViewById(R.id.time_text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            ActivityStreamItem si = getItem(position);

            String title = si.getObject().getObjectType();
            String desc = si.getObject().getSummary();
            String objectType = si.getObject().getObjectType();
            
            if ("thread-topic".equals(objectType)) {
            	title = "Topic: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_thread_topic);
            } else if ("thread-post".equals(objectType)) {
            	title = "Re: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_thread_post);
            } else if ("grade".equals(objectType)) {
            	title = "Grade: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_grade);
            } else if ("dropbox-submission".equals(objectType)) {
            	title = "Dropbox: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_dropbox_submission);
            } else if ("exam-submission".equals(objectType)) {
            	title = "Exam: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_exam_submission);
            } else if ("remark".equals(objectType)) {
            	title = "Remark: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_remark);
            } 
            if (title == null) title = "";
            if (desc == null) desc = "";
            holder.titleText.setText(title);
            holder.descriptionText.setText(Html.fromHtml(desc),BufferType.SPANNABLE);
            holder.timeText.setText(prettyTimeFormatter.format(si.getPostedTime().getTime()));
            
            //holder.iconPlaceholder.setText(si.getObject().getObjectType().substring(0, 1));
            return convertView;
        }
    	
    }
}