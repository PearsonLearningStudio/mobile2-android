package com.ecollege.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Months;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.ActivityFeedAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.adapter.UberItem.UberItemType;
import com.ecollege.android.adapter.WaitingForApiAdapter;
import com.ecollege.android.tasks.TaskPostProcessor;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.services.activities.FetchMyWhatsHappeningFeed;
import com.google.inject.Inject;

public class HomeActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.array.home_navigation_items) String[] homeNavigationItems;
	@InjectResource(R.string.last_updated_s) String lastUpdatedFormat;
	@InjectResource(R.string.last_updated_date_format) String lastUpdatedDateFormatString;
	@InjectView(R.id.navigation_dropdown) Spinner navigationSpinner;
	@InjectView(R.id.reload_button) Button reloadButton;
	
	protected static final int ACTIVITY_POSITION = 0;
	protected static final int UPCOMING_POSITION = 1;
	
	protected ECollegeClient client;
	LayoutInflater mInflater;
	private View lastUpdatedHeader;
	private TextView lastUpdatedText;
	private SimpleDateFormat lastUpdatedDateFormat;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
        lastUpdatedDateFormat = new SimpleDateFormat(lastUpdatedDateFormatString);
        setUpNavigation();
        
        if (savedInstanceState != null) {
        	canLoadMoreActivites = savedInstanceState.getBoolean("canLoadMoreActivites", true);
        	upcomingFeedLastUpdated = savedInstanceState.getLong("upcomingFeedLastUpdated");
        	activityFeedLastUpdated = savedInstanceState.getLong("activityFeedLastUpdated");
        }
        
        boolean showWhatsDue = prefs.getBoolean("showWhatsDue", true);
        if (showWhatsDue) {
        	navigationSpinner.setSelection(UPCOMING_POSITION);
        } else {
        	navigationSpinner.setSelection(ACTIVITY_POSITION);
        }
        
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		    	if (navigationSpinner.getSelectedItemPosition() == UPCOMING_POSITION) {
		    		//TODO: reload what's due
		    	} else {
		    		reloadActivityFeed();
		    	}
			}
		});
        
        buildLastUpdatedHeader();
        loadAndDisplayListForSelectedType();
    }
    
	protected void buildLastUpdatedHeader() {
		lastUpdatedHeader = getLayoutInflater().inflate(R.layout.last_updated_view, null);
		lastUpdatedText = (TextView) lastUpdatedHeader.findViewById(R.id.last_updated_text);
    	getListView().addHeaderView(lastUpdatedHeader, null, false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("canLoadMoreActivites", canLoadMoreActivites);
		outState.putLong("upcomingFeedLastUpdated", upcomingFeedLastUpdated);
		outState.putLong("whatsHappeneingLastUpdated", activityFeedLastUpdated);
    	if (navigationSpinner != null) {
    		prefs.edit().putBoolean("showWhatsDue", upcomingIsSelected()).commit();
    	}
	}

    protected void setUpNavigation() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, R.layout.transparent_spinner_text_view, homeNavigationItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        navigationSpinner.setAdapter(adapter);
		navigationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				navigationChanged(arg2);
			}

			public void onNothingSelected(AdapterView<?> arg0) { }
		});
	}

    protected void navigationChanged(int newPosition) {
    	loadAndDisplayListForSelectedType();
	}

    protected void loadAndDisplayListForSelectedType() {
    	ListAdapter chosenAdapter;
    	String formattedLastUpdated = getString(R.string.never);
    	if (upcomingIsSelected()) {
    		chosenAdapter = createOrReturnWhatsHappeningAdapter();
    		if (upcomingFeedLastUpdated != 0) {
    			formattedLastUpdated = lastUpdatedDateFormat.format(new Date(upcomingFeedLastUpdated));
    		}
    	} else {
    		chosenAdapter = createOrReturnActivitiesAdapter();
    		if (activityFeedLastUpdated != 0) {
    			formattedLastUpdated = lastUpdatedDateFormat.format(new Date(activityFeedLastUpdated));
    		}
    	}
    	lastUpdatedText.setText(String.format(lastUpdatedFormat, formattedLastUpdated));
    	setListAdapter(chosenAdapter);
    }

	private WaitingForApiAdapter upcomingAdapter;
    protected ListAdapter createOrReturnWhatsHappeningAdapter() {
    	if (upcomingAdapter == null) {
    		upcomingAdapter = new WaitingForApiAdapter(this);
    	}
    	return upcomingAdapter;
    }

	private ActivityFeedAdapter activityFeedAdapter;
	private boolean canLoadMoreActivites = true;
	private long activityFeedLastUpdated;
	private long upcomingFeedLastUpdated;
	
    private ListAdapter createOrReturnActivitiesAdapter() {
    	if (activityFeedAdapter == null) {
    		activityFeedAdapter = new ActivityFeedAdapter(this,canLoadMoreActivites);
    		fetchActivityFeed();
    	}
    	return activityFeedAdapter;
    }
    
    protected void fetchActivityFeed() {
    	fetchActivityFeed(null);
    }
    
    protected void reloadActivityFeed() {
    	CacheConfiguration cacheConfiguration = new CacheConfiguration(true, true, true, true);
    	if (canLoadMoreActivites) {
    		GregorianCalendar fetchSince = new GregorianCalendar();
    		fetchSince.add(Calendar.DAY_OF_YEAR, -14);
        	buildService(new FetchMyWhatsHappeningFeed(fetchSince))
        		.setPostProcessor(new ActivityFeedPostProcessor<FetchMyWhatsHappeningFeed>())
        		.configureCaching(cacheConfiguration)
        		.execute();
    	} else {
    		buildService(new FetchMyWhatsHappeningFeed())
    			.setPostProcessor(new ActivityFeedPostProcessor<FetchMyWhatsHappeningFeed>())
        		.configureCaching(cacheConfiguration)
    			.execute();	
    	}
    }
    
    protected void fetchActivityFeed(CacheConfiguration cacheConfiguration) {
    	activityFeedAdapter.beginLoading();
    	
    	if (null == cacheConfiguration) {
    		cacheConfiguration = new CacheConfiguration(); // default hits the most caches
    	}
    	if (canLoadMoreActivites) {
    		GregorianCalendar fetchSince = new GregorianCalendar();
    		fetchSince.add(Calendar.DAY_OF_YEAR, -14);
        	buildService(new FetchMyWhatsHappeningFeed(fetchSince))
        		.setPostProcessor(new ActivityFeedPostProcessor<FetchMyWhatsHappeningFeed>())
        		.configureCaching(cacheConfiguration)
        		.disableTitlebarBusyIndicator()
        		.execute();
    	} else {
    		buildService(new FetchMyWhatsHappeningFeed())
    			.setPostProcessor(new ActivityFeedPostProcessor<FetchMyWhatsHappeningFeed>())
        		.configureCaching(cacheConfiguration)
    			.disableTitlebarBusyIndicator()
    			.execute();	
    	}
    }
    
    protected List<ActivityStreamItem> activityItems;
    
    public void onServiceCallSuccess(FetchMyWhatsHappeningFeed service) {
    	
    	if (service.getResult().size() == 0 && canLoadMoreActivites) {
    		canLoadMoreActivites = false; //load the extra activities if no data in last 14 days
    		fetchActivityFeed();
    		return;
    	}
    	
    	activityItems = service.getResult();
    	activityFeedAdapter.updateItems(activityItems,canLoadMoreActivites);
    	activityFeedLastUpdated = service.getCompletedAt();
    	loadAndDisplayListForSelectedType();
    }

    public void onServiceCallException(FetchMyWhatsHappeningFeed service, Exception ex) {
    	activityFeedAdapter.hasError();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	@SuppressWarnings("unchecked")
		UberItem<ActivityStreamItem> item = (UberItem<ActivityStreamItem>)l.getItemAtPosition(position);
    	
    	if (item.getItemType() == UberItemType.LOAD_MORE_ITEM) {
    		canLoadMoreActivites = false;
    		fetchActivityFeed();
    		return;
    	} else if (item.getItemType() == UberItemType.DATA_ITEM) {
    		ActivityStreamItem si = item.getDataItem();
    		
            String objectType = si.getObject().getObjectType();
            
            if ("thread-topic".equals(objectType)) {
            	long topicId = Long.parseLong(si.getObject().getReferenceId());
            	Intent i = new Intent(this,UserTopicActivity.class);
            	i.putExtra(UserTopicActivity.TOPIC_ID_EXTRA, topicId);
            	startActivity(i);
            } else if ("thread-post".equals(objectType)) {
            	long responseId = Long.parseLong(si.getObject().getReferenceId());
            	Intent i = new Intent(this,UserResponseActivity.class);
            	i.putExtra(UserResponseActivity.RESPONSE_ID_EXTRA, responseId);
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
    	
    }
    
    private boolean upcomingIsSelected() {
    	return (navigationSpinner.getSelectedItemPosition() == UPCOMING_POSITION);
	}

    static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timeText;
        TextView courseTitleText;
        ImageView icon;
    }
    
    private class ActivityFeedPostProcessor<ServiceT extends FetchMyWhatsHappeningFeed> extends TaskPostProcessor<ServiceT> {

		@Override
		public ServiceT onPostProcess(ServiceT service) {
			ServiceT result = service;
			if (result != null && result.getResult() != null) {
				for (ActivityStreamItem asi : result.getResult()) {
					if (asi.getPostedTime() != null) {
						DateTime now = new DateTime();
						DateTime postedTime = new DateTime(asi.getPostedTime());
						
						//int daysBetween = Days.daysBetween(postedTime, now).getDays();
						int monthsBetween = Months.monthsBetween(postedTime, now).getMonths();
						
						if (monthsBetween <= 1) {
							asi.setTag("Past 30 days");
						} else {
							asi.setTag("Over " + monthsBetween  + " months ago");
						}						
					}
				}
			}
			return result;
		}
    }
}