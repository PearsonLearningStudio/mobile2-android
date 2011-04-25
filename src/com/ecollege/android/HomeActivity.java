package com.ecollege.android;

import java.util.Calendar;
import java.util.Collections;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.ActivityFeedAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.adapter.UberItem.UberItemType;
import com.ecollege.android.adapter.UpcomingEventsAdapter;
import com.ecollege.android.tasks.TaskPostProcessor;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.UpcomingEventItem;
import com.ecollege.api.model.UpcomingEventItem.UpcomingEventType;
import com.ecollege.api.services.activities.FetchMyWhatsHappeningFeed;
import com.ecollege.api.services.upcoming.FetchMyUpcomingEventsService;
import com.google.inject.Inject;

public class HomeActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.array.home_navigation_items) String[] homeNavigationItems;
	@InjectView(R.id.navigation_dropdown) Spinner navigationSpinner;
	@InjectView(R.id.reload_button) Button reloadButton;
	
	protected static final int ACTIVITY_POSITION = 0;
	protected static final int UPCOMING_POSITION = 1;
	
	protected ECollegeClient client;
	LayoutInflater mInflater;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
        setUpNavigation();
        
        if (savedInstanceState != null) {
        	canLoadMoreActivites = savedInstanceState.getBoolean("canLoadMoreActivites", true);
        }
        
        boolean showWhatsDue = prefs.getBoolean("showWhatsDue", true);
        if (showWhatsDue) {
        	navigationSpinner.setSelection(UPCOMING_POSITION);
        } else {
        	navigationSpinner.setSelection(ACTIVITY_POSITION);
        }
        
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
	    		reloadCurrentFeed();
			}
		});
        
        loadAndDisplayListForSelectedType();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("canLoadMoreActivites", canLoadMoreActivites);
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
    	if (upcomingIsSelected()) {
    		chosenAdapter = createOrReturnUpcomingAdapter();
    	} else {
    		chosenAdapter = createOrReturnActivitiesAdapter();
    	}
    	setListAdapter(chosenAdapter);
    }

	private UpcomingEventsAdapter upcomingEventsAdapter;
	private boolean canLoadMoreUpcomingEvents = true;
	
    protected ListAdapter createOrReturnUpcomingAdapter() {
    	if (upcomingEventsAdapter == null) {
    		upcomingEventsAdapter = new UpcomingEventsAdapter(this,canLoadMoreUpcomingEvents);
    		fetchUpcomingEvents();
    	}
    	return upcomingEventsAdapter;
    }

	private ActivityFeedAdapter activityFeedAdapter;
	private boolean canLoadMoreActivites = true;
	
    private ListAdapter createOrReturnActivitiesAdapter() {
    	if (activityFeedAdapter == null) {
    		activityFeedAdapter = new ActivityFeedAdapter(this,canLoadMoreActivites);
    		fetchActivityFeed();
    	}
    	return activityFeedAdapter;
    }

    protected void fetchUpcomingEvents() {
    	fetchUpcomingEvents(null);
    }
    
    protected void fetchActivityFeed() {
    	fetchActivityFeed(null);
    }
    
    protected void reloadCurrentFeed() {
    	CacheConfiguration cacheConfiguration = new CacheConfiguration(true, true, true, true);
    	
    	if (upcomingIsSelected()) {
    		fetchUpcomingEvents(cacheConfiguration);
    	} else {
    		fetchActivityFeed(cacheConfiguration);
    	}
    }
    
    protected void fetchUpcomingEvents(CacheConfiguration cacheConfiguration) {
    	upcomingEventsAdapter.beginLoading();
    	
    	if (null == cacheConfiguration) {
    		cacheConfiguration = new CacheConfiguration(); // default hits the most caches
    	}
    	if (canLoadMoreUpcomingEvents) {
    		GregorianCalendar fetchUntil = new GregorianCalendar();
    		fetchUntil.add(Calendar.DAY_OF_YEAR, +14);
        	buildService(new FetchMyUpcomingEventsService(fetchUntil))
        		.setPostProcessor(new UpcomingEventsPostProcessor<FetchMyUpcomingEventsService>())
        		.configureCaching(cacheConfiguration)
        		.disableTitlebarBusyIndicator()
        		.execute();
    	} else {
    		buildService(new FetchMyUpcomingEventsService())
    			.setPostProcessor(new UpcomingEventsPostProcessor<FetchMyUpcomingEventsService>())
        		.configureCaching(cacheConfiguration)
    			.disableTitlebarBusyIndicator()
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
    	activityFeedAdapter.setLastUpdatedAt(service.getCompletedAt());
    	activityFeedAdapter.updateItems(activityItems,canLoadMoreActivites);
    	loadAndDisplayListForSelectedType();
    }

    public void onServiceCallException(FetchMyWhatsHappeningFeed service, Exception ex) {
    	activityFeedAdapter.hasError();
    }
    

    protected List<UpcomingEventItem> upcomingEventItems;
    
    public void onServiceCallSuccess(FetchMyUpcomingEventsService service) {
    	
    	if (service.getResult().size() == 0 && canLoadMoreUpcomingEvents) {
    		canLoadMoreUpcomingEvents = false; //load the extra activities if no data in next 14 days
    		fetchUpcomingEvents();
    		return;
    	}
    	
    	upcomingEventItems = service.getResult();
    	Collections.sort(upcomingEventItems);
    	
    	upcomingEventsAdapter.setLastUpdatedAt(service.getCompletedAt());
    	upcomingEventsAdapter.updateItems(upcomingEventItems,canLoadMoreUpcomingEvents);
    	loadAndDisplayListForSelectedType();
    }

    public void onServiceCallException(FetchMyUpcomingEventsService service, Exception ex) {
    	upcomingEventsAdapter.hasError();
    }    
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	if (upcomingIsSelected()) {
        	@SuppressWarnings("unchecked")
    		UberItem<UpcomingEventItem> item = (UberItem<UpcomingEventItem>)l.getItemAtPosition(position);
    		
        	if (item.getItemType() == UberItemType.LOAD_MORE_ITEM) {
        		canLoadMoreUpcomingEvents = false;
        		fetchUpcomingEvents();
        		return;
        	} else if (item.getItemType() == UberItemType.DATA_ITEM) {
        		
        		if (item.getDataItem().getEventType() == UpcomingEventType.Thread) {
                	long threadId = item.getDataItem().getThreadId();
                	Course course = app.getCourseById(item.getDataItem().getCourseId());
                	
                	Intent i = new Intent(this,CourseThreadActivity.class);
                	i.putExtra(CourseThreadActivity.THREAD_ID_EXTRA, threadId);
                	i.putExtra(CourseThreadActivity.COURSE_EXTRA, course);
                	i.putExtra(CourseThreadActivity.SCHEDULE_EXTRA, UpcomingEventsAdapter.getScheduleText(item.getDataItem()));
                	startActivity(i);
        		} else if (item.getDataItem().getEventType() == UpcomingEventType.Html) {
                	long htmlId = item.getDataItem().getMultimediaId();
                	Course course = app.getCourseById(item.getDataItem().getCourseId());
                	
                	Intent i = new Intent(this,HtmlContentActivity.class);
                	i.putExtra(HtmlContentActivity.HTML_ID_EXTRA, htmlId);
                	i.putExtra(HtmlContentActivity.COURSE_EXTRA, course);
                	i.putExtra(HtmlContentActivity.SCHEDULE_EXTRA, UpcomingEventsAdapter.getScheduleText(item.getDataItem()));
                	i.putExtra(HtmlContentActivity.TITLE_EXTRA, item.getDataItem().getTitle());
                	
                	startActivity(i);
        		}
        		
        	}
        	
    	} else {
    		
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
                	i.putExtra("title", si.getTarget().getTitle());
                	startActivity(i);
                } else if ("exam-submission".equals(objectType)) {
                } else if ("remark".equals(objectType)) {
                } 
        	}
    		
    	} 
    }
    
    private boolean upcomingIsSelected() {
    	return (navigationSpinner.getSelectedItemPosition() == UPCOMING_POSITION);
	}
    
    private class UpcomingEventsPostProcessor<ServiceT extends FetchMyUpcomingEventsService> extends TaskPostProcessor<ServiceT> {

		@Override
		public ServiceT onPostProcess(ServiceT service) {
			ServiceT result = service;
			if (result != null && result.getResult() != null) {
				for (UpcomingEventItem uei : result.getResult()) {
					if (uei.getWhen() != null && uei.getWhen().getTime() != null) {
						
						long dt = uei.getWhen().getTime().getTime().getTime();
						
						if (dt < getTodayPlus(1)) {
							uei.setTag("Today");
						} else if (dt < getTodayPlus(2)) {
							uei.setTag("Tomorrow");
						} else if (dt < getTodayPlus(3)) {
							uei.setTag("In 2 Days");
						} else if (dt < getTodayPlus(4)) {
							uei.setTag("In 3 Days");
						} else if (dt < getTodayPlus(5)) {
							uei.setTag("In 4 Days");
						} else if (dt < getTodayPlus(6)) {
							uei.setTag("In 5 Days");
						} else {
							uei.setTag("Later");
						}					
					}
				}
			}
			return result;
		}
		
		private long getTodayPlus(int day) {
			Date now = new Date();
			Date result = new Date(now.getYear(),now.getMonth(),now.getDate() + day); //beginning of today
			return result.getTime();
		}
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