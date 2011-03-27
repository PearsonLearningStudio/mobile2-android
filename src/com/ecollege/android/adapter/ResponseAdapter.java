package com.ecollege.android.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecollege.android.R;
import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.model.DiscussionResponse;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.User;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ocpsoft.pretty.time.PrettyTime;

public class ResponseAdapter extends ArrayAdapter<UserDiscussionResponse> {
	
	final private class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView authorText;
        ImageView authorIcon;
        TextView timeText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
        TextView descriptionText;
    }

	final private PrettyTime prettyTimeFormatter = new PrettyTime();
	final private LayoutInflater viewInflater;
	
	protected boolean loading;
	
	public ResponseAdapter(Context context, List<UserDiscussionResponse> topicList) {
		super(context, R.layout.user_topic_item, topicList);
		viewInflater = LayoutInflater.from(context);
	}
	
	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	@Override public int getViewTypeCount() { return (loading) ? 1 : super.getViewTypeCount(); }
	@Override public boolean isEnabled(int position) { return (loading) ? false : super.isEnabled(position); }
	@Override public boolean areAllItemsEnabled() { return (loading) ? false : super.areAllItemsEnabled(); }
	@Override public int getItemViewType(int position) {
		if (loading) {
			return IGNORE_ITEM_VIEW_TYPE;
		} else {
			return super.getItemViewType(position);
		}
	}
	public int getCount() { return (loading) ? 1 : super.getCount(); }
	public UserDiscussionResponse getItem(int position) { return (loading) ? null : super.getItem(position); }
	public long getItemId(int position) { return (loading) ? 0 : super.getItemId(position); }
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (loading) return viewInflater.inflate(R.layout.loading_item, null); 
		
		if (convertView == null) {
			convertView = viewInflater.inflate(R.layout.user_topic_item, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
			holder.timeText = (TextView) convertView.findViewById(R.id.time_text);
			holder.authorText = (TextView) convertView.findViewById(R.id.author_text);
			holder.authorIcon = (ImageView) convertView.findViewById(R.id.author_icon);
			holder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
			holder.totalResponseCountText = (TextView) convertView.findViewById(R.id.total_response_count_text);
			holder.unreadResponseCountText = (TextView) convertView.findViewById(R.id.unread_response_count_text);
			holder.userResponseCountText = (TextView) convertView.findViewById(R.id.user_response_count_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		UserDiscussionResponse userResponse = getItem(position);
		DiscussionResponse response = userResponse.getResponse();
		ResponseCount responseCount = userResponse.getChildResponseCounts();
		User author = response.getAuthor();
		
		holder.timeText.setVisibility(View.VISIBLE);
		holder.authorText.setVisibility(View.VISIBLE);
		holder.authorIcon.setVisibility(View.VISIBLE);
		holder.descriptionText.setVisibility(View.VISIBLE);
		
		holder.titleText.setText(Html.fromHtml(response.getTitle()).toString());
		holder.authorText.setText(author.getFirstName() + " " + author.getLastName());
		holder.descriptionText.setText(Html.fromHtml(response.getDescription()));
		holder.timeText.setText(prettyTimeFormatter.format(response.getPostedDate().getTime()));
		ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(getContext(),
				holder.icon,
				holder.unreadResponseCountText,
				holder.totalResponseCountText,
				holder.userResponseCountText
		);
		responseCountViewHelper.setResponseCount(responseCount);
		return convertView;
	}

	
}

