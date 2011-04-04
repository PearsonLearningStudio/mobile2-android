package com.ecollege.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ecollege.android.R;

public class WaitingForApiAdapter extends ArrayAdapter<String> {   
	
    private static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
    }
    
	public WaitingForApiAdapter(Context c) {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, null);

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