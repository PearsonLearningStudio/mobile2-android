package com.ecollege.android.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.R;
import com.ecollege.android.util.DateTimeUtil;
import com.ecollege.api.model.Course;
import com.ecollege.api.model.UpcomingEventItem;
import com.ecollege.api.model.UpcomingEventItem.CategoryType;
import com.ecollege.api.model.UpcomingEventItem.UpcomingEventType;

public class UpcomingEventsAdapter extends UberAdapter<UpcomingEventItem> {

	public UpcomingEventsAdapter(Context context, boolean canLoadMore) {
		super(context, true, false, canLoadMore);
	}
	
	@Override
	protected Object groupIdFunction(UpcomingEventItem item) {
		UpcomingEventItem uei = (UpcomingEventItem)item;
		if (uei.getTag() != null) return uei.getTag().toString();
		return "Unknown";
	}
	
	
    private static class ViewHolder {
        TextView titleText;
        TextView scheduleText;
        TextView courseTitleText;
        ImageView icon;
    }
    
	@Override
	protected View getDataItemView(View convertView, ViewGroup parent, UberItem<UpcomingEventItem> item) {

        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
            holder.scheduleText = (TextView) convertView.findViewById(R.id.schedule_text);
            holder.courseTitleText = (TextView) convertView.findViewById(R.id.course_title_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }
        // Bind the data efficiently with the holder.
        UpcomingEventItem uei = item.getDataItem();
        
        String title = uei.getTitle();
        long courseId = uei.getCourseId();
        Course course = ((ECollegeApplication)parent.getContext().getApplicationContext()).getCourseById(courseId);
        
        holder.titleText.setText(Html.fromHtml(title).toString());
        holder.courseTitleText.setText(Html.fromHtml(course.getTitle()).toString());
        
        if (uei.getEventType() == UpcomingEventType.Html) {
        	holder.icon.setImageResource(R.drawable.ic_person);
        } else if (uei.getEventType() == UpcomingEventType.Thread) {
        	holder.icon.setImageResource(R.drawable.ic_discussions_no_responses);
        } else if (uei.getEventType() == UpcomingEventType.QuizExamTest) {
        	holder.icon.setImageResource(R.drawable.ic_grade);
        }
        
        if (uei.getWhen() == null || uei.getWhen().getTime() == null) {
        	holder.scheduleText.setText("Schedule Unknown");
        } else {
            if (uei.getCategoryType() == CategoryType.Start) {
            	holder.scheduleText.setText("Starts at " + DateTimeUtil.getLongFriendlyDate(uei.getWhen().getTime()));
            } else if (uei.getCategoryType() == CategoryType.End) {
            	holder.scheduleText.setText("Ends at " + DateTimeUtil.getLongFriendlyDate(uei.getWhen().getTime()));
            } else if (uei.getCategoryType() == CategoryType.Due) {
            	holder.scheduleText.setText("Due at " + DateTimeUtil.getLongFriendlyDate(uei.getWhen().getTime()));
            }
        }
        
        
        return convertView;	
	}
	
}

