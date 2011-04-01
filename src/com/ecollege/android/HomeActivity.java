package com.ecollege.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Months;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.HeaderAdapter;
import com.ecollege.android.adapter.LoadMoreAdapter;
import com.ecollege.android.tasks.TaskPostProcessor;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.model.ActivityStreamObject;
import com.ecollege.api.model.Course;
import com.ecollege.api.services.activities.FetchMyWhatsHappeningFeed;
import com.google.inject.Inject;
import com.ocpsoft.pretty.time.PrettyTime;

public class HomeActivity extends ECollegeListActivity {
	
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectResource(R.array.home_navigation_items) String[] homeNavigationItems;
	@InjectResource(R.string.last_updated_s) String lastUpdatedFormat;
	@InjectResource(R.string.last_updated_date_format) String lastUpdatedDateFormatString;
	@InjectView(R.id.navigation_dropdown) Spinner navigationSpinner;
	@InjectView(R.id.reload_button) Button reloadButton;
	
	protected static final int ACTIVITY_POSITION = 0;
	protected static final int WHATS_DUE_POSITION = 1;
	
	protected ECollegeClient client;
	private LayoutInflater mInflater;
	private View lastUpdatedHeader;
	private TextView lastUpdatedText;
	private SimpleDateFormat lastUpdatedDateFormat;
	
	private static final PrettyTime prettyTimeFormatter = new PrettyTime();
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
        lastUpdatedDateFormat = new SimpleDateFormat(lastUpdatedDateFormatString);
        setUpNavigation();
        
        if (savedInstanceState != null) {
        	canLoadMoreActivites = savedInstanceState.getBoolean("canLoadMoreActivites", true);
        	whatsDueLastUpdated = savedInstanceState.getLong("whatsDueLastUpdated");
        	whatsHappeningLastUpdated = savedInstanceState.getLong("whatsHappeningLastUpdated");
        }
        
        boolean showWhatsDue = prefs.getBoolean("showWhatsDue", true);
        if (showWhatsDue) {
        	navigationSpinner.setSelection(WHATS_DUE_POSITION);
        } else {
        	navigationSpinner.setSelection(ACTIVITY_POSITION);
        }
        
        reloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		    	if (navigationSpinner.getSelectedItemPosition() == WHATS_DUE_POSITION) {
		    		//TODO: reload what's due
		    	} else {
		    		reloadWhatsHappening();
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
		outState.putLong("whatsDueLastUpdated", whatsDueLastUpdated);
		outState.putLong("whatsHappeneingLastUpdated", whatsHappeningLastUpdated);
    	if (navigationSpinner != null) {
    		prefs.edit().putBoolean("showWhatsDue", whatsDueIsSelected()).commit();
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
    	if (whatsDueIsSelected()) {
    		chosenAdapter = createOrReturnWhatsHappeningAdapter();
    		if (whatsDueLastUpdated != 0) {
    			formattedLastUpdated = lastUpdatedDateFormat.format(new Date(whatsDueLastUpdated));
    		}
    	} else {
    		chosenAdapter = createOrReturnActivitiesAdapter();
    		if (whatsHappeningLastUpdated != 0) {
    			formattedLastUpdated = lastUpdatedDateFormat.format(new Date(whatsHappeningLastUpdated));
    		}
    	}
    	lastUpdatedText.setText(String.format(lastUpdatedFormat, formattedLastUpdated));
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
	private long whatsHappeningLastUpdated;
	private long whatsDueLastUpdated;
	private ActivityFeedHeaderAdapter activityHeaderAdapter;
	
    private ListAdapter createOrReturnActivitiesAdapter() {
    	if (activityAdapter == null) {
			ActivityFeedAdapter baseAdapter = new ActivityFeedAdapter(this,new ArrayList<ActivityStreamItem>());
    		activityAdapter = new LoadMoreAdapter(this, baseAdapter, canLoadMoreActivites);
    		fetchWhatsHappening();
    	}
    	return activityAdapter;
    }
    
    protected void fetchWhatsHappening() {
    	fetchWhatsHappening(null);
    }
    
    protected void reloadWhatsHappening() {
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
    
    protected void fetchWhatsHappening(CacheConfiguration cacheConfiguration) {
    	if (null == cacheConfiguration) {
    		cacheConfiguration = new CacheConfiguration(); // default hits the most caches
    	}
		activityAdapter.setIsLoadingMore(true);
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
    
    public void onServiceCallSuccess(FetchMyWhatsHappeningFeed service) {
		ActivityFeedAdapter baseAdapter = new ActivityFeedAdapter(this,service.getResult());
		activityHeaderAdapter = new ActivityFeedHeaderAdapter(this, baseAdapter);
		activityHeaderAdapter.setListHeaderCount(getListView().getHeaderViewsCount());
    	activityAdapter.update(activityHeaderAdapter,canLoadMoreActivites);
    	whatsHappeningLastUpdated = service.getCompletedAt();
    	loadAndDisplayListForSelectedType();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	if (id == LoadMoreAdapter.LOAD_MORE_ITEM_ID) {
    		canLoadMoreActivites = false;
    		fetchWhatsHappening();
    		return;
    	}
    	
    	ActivityStreamItem si = (ActivityStreamItem)activityHeaderAdapter.getItem(position);
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
    
    private boolean whatsDueIsSelected() {
    	return (navigationSpinner.getSelectedItemPosition() == WHATS_DUE_POSITION);
	}

    static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timeText;
        TextView courseTitleText;
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

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
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
    
    private class ActivityFeedHeaderAdapter extends HeaderAdapter {
    	
		public ActivityFeedHeaderAdapter(Context context, ListAdapter baseAdapter) {
			super(context, baseAdapter);
			setListHeaderCount(getListView().getHeaderViewsCount());
		}

		@Override
		protected String headerLabelFunction(Object item, int position) {
			ActivityStreamItem asi = (ActivityStreamItem)item;
			if (asi.getTag() != null) return asi.getTag().toString();
			return "Unknown";
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
                holder.courseTitleText = (TextView) convertView.findViewById(R.id.course_title_text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            ActivityStreamItem si = getItem(position);
            ActivityStreamObject ob = si.getObject();

            String title = ob.getObjectType();
            String desc = ob.getSummary();
            String objectType = ob.getObjectType();
            
            long courseId = ob.getCourseId();
            Course course = app.getCourseById(courseId);
            
            if ("thread-topic".equals(objectType)) {
            	title = "Topic: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_discussions_responses);
            } else if ("thread-post".equals(objectType)) {
            	title = "Re: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_discussions_responses);
            } else if ("grade".equals(objectType)) {
            	title = "Grade: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_grade);
            } else if ("dropbox-submission".equals(objectType)) {
            	title = "Dropbox: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_dropbox);
            } else if ("exam-submission".equals(objectType)) {
            	title = "Exam: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_exam_submission);
            } else if ("remark".equals(objectType)) {
            	title = "Remark: " + si.getTarget().getTitle();
            	holder.icon.setImageResource(R.drawable.ic_remark);
            } 
            if (title == null) title = "";
            if (desc == null) desc = "";
            holder.titleText.setText(Html.fromHtml(title));
            holder.descriptionText.setText(Html.fromHtml(desc),BufferType.SPANNABLE);
            holder.timeText.setText(prettyTimeFormatter.format(si.getPostedTime().getTime()));
            if (course != null) {
            	holder.courseTitleText.setText(Html.fromHtml(course.getTitle()));
            }
            
            return convertView;
        }
    	
    }
}