package com.ecollege.android;

import java.util.List;

import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
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
	protected ECollegeClient client;
	private LayoutInflater mInflater;
	private List<ActivityStreamItem> currentStreamItems;
	
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mInflater = getLayoutInflater();
        client = app.getClient();
        
		new ServiceCallTask<FetchMyWhatsHappeningFeed>(app,new FetchMyWhatsHappeningFeed()) {
			@Override
			protected void onSuccess(FetchMyWhatsHappeningFeed service) throws Exception {
				super.onSuccess(service);
				
				if (currentActivity.get() instanceof HomeActivity) {
					HomeActivity ha = ((HomeActivity)currentActivity.get());
					ha.currentStreamItems = service.getResult();
					ha.setListAdapter(new ActivityFeedAdapter(ha.currentStreamItems));
				}
			}
		}.execute();        
    }
    
    static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView iconPlaceholder;
    }
    
    private class ActivityFeedAdapter implements ListAdapter {
    	
    	private List<ActivityStreamItem> streamItems; 
    	
    	public ActivityFeedAdapter(List<ActivityStreamItem> streamItems) {
    		this.streamItems=streamItems;
    	}
    	
		public int getCount() {
			return streamItems.size();
		}

		public Object getItem(int position) {
			return streamItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getItemViewType(int position) {
			return 0;
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
            ActivityStreamItem si = streamItems.get(position);

            holder.titleText.setText(si.getObject().getObjectType());
            holder.descriptionText.setText(si.getObject().getSummary());
            holder.iconPlaceholder.setText(si.getObject().getObjectType().substring(0, 1));
            return convertView;
        }

		public int getViewTypeCount() {
			return 1;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isEmpty() {
			return streamItems.size() == 0;
		}

		public void registerDataSetObserver(DataSetObserver observer) {
			// do anything here?
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			// do anything here?			
		}

		public boolean areAllItemsEnabled() {
			return true;
		}

		public boolean isEnabled(int position) {
			return true;
		}
    	
    }
}