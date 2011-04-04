package com.ecollege.android.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.ecollege.android.ECollegeApplication;
import com.ecollege.android.R;
import com.ecollege.api.model.ActivityStreamItem;
import com.ecollege.api.model.ActivityStreamObject;
import com.ecollege.api.model.Course;
import com.ocpsoft.pretty.time.PrettyTime;

public class ActivityFeedAdapter extends LoadMoreAdapter {

	private Context context;
	private BaseActivityFeedAdapter baseAdapter;
	private GroupedActivityFeedAdapter groupedAdapter;
	private static final PrettyTime prettyTimeFormatter = new PrettyTime();
	
	public ActivityFeedAdapter(Context context, List<ActivityStreamItem> items, boolean canLoadMore) {
		super(context, null, canLoadMore);
		this.context = context;
		this.baseAdapter = new BaseActivityFeedAdapter(context, R.layout.activity_item, items);
		this.groupedAdapter = new GroupedActivityFeedAdapter(context, baseAdapter);
		super.update(groupedAdapter, canLoadMore);
	}
	
	
	@Override
	public void update(ListAdapter baseAdapter, boolean canLoadMore) {
		throw new RuntimeException("Cannot call update directly on ActivityFeedAdapter, use updateItems instead");
	}

	public void setCanLoadMore(boolean canLoadMore) {
		super.update(groupedAdapter, canLoadMore);
	}
	
	public void updateItems(List<ActivityStreamItem> items) {
		baseAdapter = new BaseActivityFeedAdapter(context, R.layout.activity_item, items);
		groupedAdapter.update(baseAdapter);
	}
	
	private class GroupedActivityFeedAdapter extends GroupedAdapter {

		public GroupedActivityFeedAdapter(Context context,ListAdapter baseAdapter) {
			super(context, baseAdapter,true,false);
		}
		
		@Override
		protected String groupIdFunction(Object item, int position) {
			ActivityStreamItem asi = (ActivityStreamItem)item;
			if (asi.getTag() != null) return asi.getTag().toString();
			return "Unknown";
		}
		
	}
	
    private static class ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timeText;
        TextView courseTitleText;
        ImageView icon;
    }
	
	private class BaseActivityFeedAdapter extends ArrayAdapter<ActivityStreamItem> {

		public BaseActivityFeedAdapter(Context context, int textViewResourceId,
				List<ActivityStreamItem> objects) {
			super(context, textViewResourceId, objects);
		}
		
		
		@Override
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
	
}

