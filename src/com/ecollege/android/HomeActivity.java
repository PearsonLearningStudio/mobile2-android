package com.ecollege.android;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.services.activities.FetchMyWhatsHappeningFeed;
import com.google.inject.Inject;

public class HomeActivity extends ECollegeListActivity {
	@Inject ECollegeApplication app;
	@Inject SharedPreferences prefs;
	@InjectView(R.id.radio_whats_due) RadioButton whatsDueRadioButton;
	
	protected ECollegeClient client;
	private LayoutInflater mInflater;
	private List<ActivityStreamItem> currentStreamItems;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
        updateSelectedView();
    }
    
    public void onRadioGroupCheckedChanged(View v) {
    	if (whatsDueRadioButton.isChecked()) {
    		if (prefs.getBoolean("showWhatsDue", true) != true) {
    			prefs.edit().putBoolean("showWhatsDue", true).commit();    			
    		}
    	} else {
    		if (prefs.getBoolean("showWhatsDue", true) != false) {
    			prefs.edit().putBoolean("showWhatsDue", false).commit();
    		}    		
    	}
    	updateSelectedView();
    }
    
    protected void updateSelectedView() {
    	if (prefs.getBoolean("showWhatsDue", true)) {
    		setListAdapter(new WhatsHappeningAdapter(this));
    	} else {
    		if (currentStreamItems != null) {
    			setListAdapter(new ActivityFeedAdapter(this,currentStreamItems));
    		} else {
    			setListAdapter(new ActivityFeedAdapter(this,new ArrayList<ActivityStreamItem>()));
    			fetchWhatsHappening();
    		}
    	}
    }
    
    protected void fetchWhatsHappening() {
    	new ServiceCallTask<FetchMyWhatsHappeningFeed>(app,new FetchMyWhatsHappeningFeed()) {
			@Override
			protected void onSuccess(FetchMyWhatsHappeningFeed service) throws Exception {
				super.onSuccess(service);
				
				if (currentActivity.get() instanceof HomeActivity) {
					HomeActivity ha = ((HomeActivity)currentActivity.get());
					ha.currentStreamItems = service.getResult();
					ha.updateSelectedView();
				}
			}
		}.execute();
    }
    
    static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView iconPlaceholder;
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
                holder.iconPlaceholder = (TextView) convertView.findViewById(R.id.icon_stub);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            
            holder.titleText.setText("Pending");
            holder.descriptionText.setText("Waiting for API");
            holder.iconPlaceholder.setText("!!");
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
                holder.iconPlaceholder = (TextView) convertView.findViewById(R.id.icon_stub);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            ActivityStreamItem si = getItem(position);

            holder.titleText.setText(si.getObject().getObjectType());
            holder.descriptionText.setText(si.getObject().getSummary());
            holder.iconPlaceholder.setText(si.getObject().getObjectType().substring(0, 1));
            return convertView;
        }
    	
    }
}