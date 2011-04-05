package com.ecollege.android.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.R;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.model.ActivityStreamObject;
import com.ecollege.api.model.Course;
import com.ocpsoft.pretty.time.PrettyTime;

public class ActivityFeedAdapter extends UberAdapter<ActivityStreamItem> {

	private static final PrettyTime prettyTimeFormatter = new PrettyTime();
	
	public ActivityFeedAdapter(Context context, boolean canLoadMore) {
		super(context, true, false, canLoadMore);
	}
	
	@Override
	protected Object groupIdFunction(ActivityStreamItem item) {
		ActivityStreamItem asi = (ActivityStreamItem)item;
		if (asi.getTag() != null) return asi.getTag().toString();
		return "Unknown";
	}
	
	
    private static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timeText;
        TextView courseTitleText;
        ImageView icon;
    }
    
	@Override
	protected View getDataItemView(View convertView, ViewGroup parent, UberItem<ActivityStreamItem> item) {

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
        ActivityStreamItem si = item.getDataItem();
        ActivityStreamObject ob = si.getObject();

        String title = ob.getObjectType();
        String desc = ob.getSummary();
        String objectType = ob.getObjectType();
        
        long courseId = ob.getCourseId();
        Course course = ((ECollegeApplication)parent.getContext().getApplicationContext()).getCourseById(courseId);
        
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

