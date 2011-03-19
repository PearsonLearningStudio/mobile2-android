package com.ecollege.android;

import java.util.ArrayList;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.ParentAdapterObserver;
import com.ecollege.android.adapter.ResponseAdapter;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionResponsesForTopic;

public class UserTopicActivity extends ECollegeListActivity {
	
	@InjectExtra(DiscussionsActivity.USER_TOPIC_EXTRA) protected UserDiscussionTopic userTopic;
	@InjectResource(R.string.d_total_reponses) String totalResponsesFormat;
	@InjectResource(R.string.d_total_reponse) String totalResponseFormat;
	@InjectResource(R.string.d_responses_by_you) String responsesByYouFormat;
	@InjectResource(R.string.d_response_by_you) String responseByYouFormat;
	@InjectResource(R.string.no_responses) String noResponsesString;
	@InjectView(R.id.topic_title_text) TextView topicTitleText;
	
	protected DiscussionTopic topic;
	protected ResponseAdapter responseAdapter;
	private ResponseCount responseCount;
	public LayoutInflater viewInflater;
	private UserTopicViewAdapter userTopicAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_topic);
		topic = userTopic.getTopic();
		responseCount = userTopic.getChildResponseCounts();
		viewInflater = getLayoutInflater();
		
		topicTitleText.setText(Html.fromHtml(topic.getTitle()));
		
		loadAndDisplayResponsesForTopic();
	}

	private void loadAndDisplayResponsesForTopic() {
		setListAdapter(createOrReturnResponseAdapter());
	}
	
	private ListAdapter createOrReturnResponseAdapter() {
		if (responseAdapter == null) {
			responseAdapter = new ResponseAdapter(this, new ArrayList<UserDiscussionResponse>());
			if (userTopicAdapter == null) {
				userTopicAdapter = new UserTopicViewAdapter(responseAdapter);
			}
			responseAdapter.setLoading(true);
			fetchResponsesForTopic(false);
		}
		return userTopicAdapter;
	}

	private void fetchResponsesForTopic(boolean reload) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		buildService(new FetchDiscussionResponsesForTopic(userTopic))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallSuccess(FetchDiscussionResponsesForTopic service) {
		responseAdapter.setNotifyOnChange(false);
		for (UserDiscussionResponse response : service.getResult()) {
			responseAdapter.add(response);
		}
		responseAdapter.setNotifyOnChange(true);
		responseAdapter.setLoading(false);
		responseAdapter.notifyDataSetChanged();
		loadAndDisplayResponsesForTopic();
	}
	
	protected class UserTopicViewAdapter extends BaseAdapter {
		
		final int STATIC_VIEWS = 3;
		final long FAKE_ID = 1000000000;
		
		private ResponseAdapter responseAdapter;
		private ParentAdapterObserver adapterObserver;
		public boolean isLoading;

		public UserTopicViewAdapter(ResponseAdapter responseAdapter) {
			assert(responseAdapter != null);
			this.responseAdapter = responseAdapter;
			this.adapterObserver = new ParentAdapterObserver(this);
			responseAdapter.registerDataSetObserver(adapterObserver);
		}
		
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}
		
		@Override public boolean areAllItemsEnabled() { return false; }
		@Override public int getViewTypeCount() { return responseAdapter.getViewTypeCount() + STATIC_VIEWS; }
		@Override public boolean hasStableIds() { return responseAdapter.hasStableIds() && true; }
		@Override public boolean isEnabled(int position) {
			if (position < STATIC_VIEWS) return false;
			return responseAdapter.isEnabled(position - STATIC_VIEWS);
		}
		@Override public int getItemViewType(int position) {
			if (position < STATIC_VIEWS) {
				return getViewTypeCount() - (position + 1);
			} else {
				return responseAdapter.getItemViewType(position - STATIC_VIEWS);
			}
		}
		public int getCount() { return responseAdapter.getCount() + STATIC_VIEWS; }
		public Object getItem(int position) { return (position < STATIC_VIEWS) ? null : responseAdapter.getItem(position - STATIC_VIEWS); }
		public long getItemId(int position) { return (position < STATIC_VIEWS) ? position + FAKE_ID : responseAdapter.getItemId(position - STATIC_VIEWS); }

		public View getView(int position, View convertView, ViewGroup parent) {
			switch (position) {
			case 0 : return getViewForTopic(convertView);
			case 1 : return getViewForDescription(convertView);
			case 2 : return getViewForPostItem(convertView);
			default : return responseAdapter.getView(position - STATIC_VIEWS, convertView, parent);
			}
		}
		
		public class TopicViewHolder {
			public TextView topicTitleText;
			public ImageView topicIcon;
			public TextView userTopicTitleText;
			public TextView totalResponseCountText;
			public TextView unreadResponseCountText;
			public TextView userResponseCountText;
		}
		
		private View getViewForTopic(View convertView) {
			TopicViewHolder holder;
			if (convertView == null) {
				holder = new TopicViewHolder();
				convertView = viewInflater.inflate(R.layout.user_topic_item, null);
				holder.topicIcon = (ImageView)convertView.findViewById(R.id.icon);
				holder.userTopicTitleText = (TextView)convertView.findViewById(R.id.title_text);
				holder.totalResponseCountText = (TextView)convertView.findViewById(R.id.total_response_count_text);
				holder.unreadResponseCountText = (TextView)convertView.findViewById(R.id.unread_response_count_text);
				holder.userResponseCountText = (TextView)convertView.findViewById(R.id.user_response_count_text);
				
				convertView.setTag(holder);
			} else {
				holder = (TopicViewHolder)convertView.getTag();
			}
			
			String htmlSafeTitle = Html.fromHtml(topic.getTitle()).toString();
			holder.userTopicTitleText.setText(htmlSafeTitle);
			
			String correctFormat = "%d";
			if (responseCount.getUnreadResponseCount() == 0) {
				holder.unreadResponseCountText.setVisibility(View.GONE);
			} else {
				holder.unreadResponseCountText.setText(Long.toString(responseCount.getUnreadResponseCount()));
				holder.unreadResponseCountText.setVisibility(View.VISIBLE);
			}
			
			if (responseCount.getTotalResponseCount() == 0) {
				holder.totalResponseCountText.setText(noResponsesString);
			} else {
				correctFormat = (responseCount.getTotalResponseCount() == 1) ? totalResponseFormat : totalResponsesFormat; 
				holder.totalResponseCountText.setText(String.format(correctFormat, responseCount.getTotalResponseCount()));
			}
			
			if (responseCount.getPersonalResponseCount() == 0) {
				holder.userResponseCountText.setVisibility(View.GONE);
			} else {
				correctFormat = (responseCount.getPersonalResponseCount() == 1) ? responseByYouFormat : responsesByYouFormat; 
				holder.userResponseCountText.setText(String.format(correctFormat, responseCount.getPersonalResponseCount()));
				holder.userResponseCountText.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
		
		public class ExpandableDescriptionHolder {
			public TextView descriptionText;
		}
		
		private View getViewForDescription(View convertView) {
			ExpandableDescriptionHolder holder;
			if (convertView == null) {
				holder = new ExpandableDescriptionHolder();
				convertView = viewInflater.inflate(R.layout.expandable_description_item, null);
				holder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
				convertView.setTag(holder);
			} else {
				holder = (ExpandableDescriptionHolder) convertView.getTag();
			}
			holder.descriptionText.setText(Html.fromHtml(topic.getDescription()));
			return convertView;
		}
		
		public class ExpandablePostViewHolder {
			public TextView contractedTextView;
		}

		private View getViewForPostItem(View convertView) {
			ExpandablePostViewHolder holder;
			if (convertView == null) {
				holder = new ExpandablePostViewHolder();
				convertView = viewInflater.inflate(R.layout.expandable_post_response_item, null);
				holder.contractedTextView = (TextView) convertView.findViewById(R.id.post_response_text);
				
				convertView.setTag(holder);
			} else {
				holder = (ExpandablePostViewHolder) convertView.getTag();
			}
			return convertView;
		}

	}

}
