package com.ecollege.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecollege.android.R;
import com.ecollege.android.adapter.UberItem.UberItemType;
import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.model.ContainerInfo;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionTopic;

public class TopicsAdapter extends UberAdapter<UserDiscussionTopic> {

	public TopicsAdapter(Context context, TopicAdapterMode mode) {
		super(context,mode == TopicAdapterMode.GROUP_BY_COURSE_FILTER_INACTIVE
				,mode == TopicAdapterMode.GROUP_BY_COURSE_FILTER_INACTIVE,false);
	}
	
	static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
    }
    
	static class FooterViewHolder {
		TextView linkText;
	}
	
	public enum TopicAdapterMode {
		GROUP_BY_COURSE_FILTER_INACTIVE,
		NO_GROUP_NO_FILTER
	}
	
	public class CourseGroupId {
		
		private long courseId;
		private String courseTitle;
		
		public CourseGroupId(long courseId, String courseTitle) {
			this.courseId = courseId;
			this.courseTitle = courseTitle;
		}
		
		public long getCourseId() {
			return courseId;
		}
		
		@Override
		public String toString() {
			return courseTitle;
		}
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof CourseGroupId)) return false;
			return ((CourseGroupId)o).getCourseId() == getCourseId();
		}
	}
	
	@Override
	public boolean isEnabled(int position) {
		UberItem<UserDiscussionTopic> item = getItem(position);
		
		if (item.getItemType() == UberItemType.FOOTER) {
			return true;
		} else {
			return super.isEnabled(position);
		}
	}
	
	@Override
	protected Object groupIdFunction(UserDiscussionTopic item) {
		UserDiscussionTopic userTopic = (UserDiscussionTopic)item;
		DiscussionTopic topic = userTopic.getTopic();
		ContainerInfo info = topic.getContainerInfo();
		
		return new CourseGroupId(info.getCourseId(), Html.fromHtml(info.getCourseTitle()).toString());
	}
	
	@Override
	protected View getFooterView(int position, View convertView,
			ViewGroup parent, Object groupId) {
		FooterViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)parent.getContext()).getLayoutInflater().inflate(R.layout.see_all_discussions, null);

            holder = new FooterViewHolder();
            holder.linkText = (TextView) convertView.findViewById(R.id.see_all_text);
            convertView.setTag(holder);
        } else {
            holder = (FooterViewHolder) convertView.getTag();
        }
        
        holder.linkText.setText("See all topics for " + groupId.toString());
        return convertView;
	}
	
	@Override
	protected View getDataItemView(View convertView, ViewGroup parent,
			UberItem<UserDiscussionTopic> item) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.user_topic_item, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
			holder.totalResponseCountText = (TextView) convertView.findViewById(R.id.total_response_count_text);
			holder.unreadResponseCountText = (TextView) convertView.findViewById(R.id.unread_response_count_text);
			holder.userResponseCountText = (TextView) convertView.findViewById(R.id.user_response_count_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		UserDiscussionTopic userTopic = item.getDataItem();
		DiscussionTopic topic = userTopic.getTopic();
		ResponseCount responseCount = userTopic.getChildResponseCounts();
		holder.titleText.setText(Html.fromHtml(topic.getTitle()).toString());
		
		ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(
				context,
				holder.icon,
				holder.unreadResponseCountText,
				holder.totalResponseCountText,
				holder.userResponseCountText
		);
		responseCountViewHelper.setResponseCount(responseCount);
		
		return convertView;
	}

	
}