package com.ecollege.android;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.DiscussionResponse;
import com.ecollege.api.model.DiscussionTopic;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.User;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionResponsesForTopic;
import com.ocpsoft.pretty.time.PrettyTime;

public class UserTopicActivity extends ECollegeListActivity {
	
	@InjectExtra(DiscussionsActivity.USER_TOPIC_EXTRA) protected UserDiscussionTopic userTopic;
	@InjectView(R.id.topic_title_text) protected TextView topicTitleText;
    @InjectView(R.id.icon) protected ImageView topicIcon;
    @InjectView(R.id.title_text) protected TextView userTopicTitleText;
    @InjectView(R.id.description_text) protected TextView descriptionText;
    @InjectView(R.id.total_response_count_text) protected TextView totalResponseCountText;
    @InjectView(R.id.unread_response_count_text) protected TextView unreadResponseCountText;
    @InjectView(R.id.user_response_count_text) protected TextView userResponseCountText;
	@InjectResource(R.string.d_total_reponses) String totalResponsesFormat;
	@InjectResource(R.string.d_total_reponse) String totalResponseFormat;
	@InjectResource(R.string.d_responses_by_you) String responsesByYouFormat;
	@InjectResource(R.string.d_response_by_you) String responseByYouFormat;
	@InjectResource(R.string.no_responses) String noResponsesString;
	
	protected DiscussionTopic topic;
	protected ResponseAdapter responseAdapter;
	public LayoutInflater viewInflater;
	
	private static PrettyTime prettyTimeFormatter = new PrettyTime();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_topic);
		viewInflater = getLayoutInflater();
		topic = userTopic.getTopic();
		ResponseCount responseCount = userTopic.getChildResponseCounts();
		String htmlSafeTitle = Html.fromHtml(topic.getTitle()).toString();
		topicTitleText.setText(htmlSafeTitle);
		userTopicTitleText.setText(htmlSafeTitle);
		descriptionText.setText(Html.fromHtml(topic.getDescription()));
		
		String correctFormat = "%d";
		if (responseCount.getUnreadResponseCount() == 0) {
			unreadResponseCountText.setVisibility(View.GONE);
		} else {
			unreadResponseCountText.setText(Long.toString(responseCount.getUnreadResponseCount()));
			unreadResponseCountText.setVisibility(View.VISIBLE);
		}
		
		if (responseCount.getTotalResponseCount() == 0) {
			totalResponseCountText.setText(noResponsesString);
		} else {
			correctFormat = (responseCount.getTotalResponseCount() == 1) ? totalResponseFormat : totalResponsesFormat; 
			totalResponseCountText.setText(String.format(correctFormat, responseCount.getTotalResponseCount()));
		}
		
		if (responseCount.getPersonalResponseCount() == 0) {
			userResponseCountText.setVisibility(View.GONE);
		} else {
			correctFormat = (responseCount.getPersonalResponseCount() == 1) ? responseByYouFormat : responsesByYouFormat; 
			userResponseCountText.setText(String.format(correctFormat, responseCount.getPersonalResponseCount()));
			userResponseCountText.setVisibility(View.VISIBLE);
		}
		
		loadAndDisplayResponsesForTopic();
	}

	private void loadAndDisplayResponsesForTopic() {
		setListAdapter(createOrReturnResponseAdapter());
	}
	
	private ListAdapter createOrReturnResponseAdapter() {
		if (responseAdapter == null) {
			responseAdapter = new ResponseAdapter(this, new ArrayList<UserDiscussionResponse>());
			fetchResponsesForTopic(false);
		}
		return responseAdapter;
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
		responseAdapter.notifyDataSetChanged();
		loadAndDisplayResponsesForTopic();
	}


	static class ViewHolder {
        ImageView icon;
        TextView titleText;
        TextView authorText;
        TextView timeText;
        TextView totalResponseCountText;
        TextView unreadResponseCountText;
        TextView userResponseCountText;
        TextView descriptionText;
    }
	
	protected class ResponseAdapter extends ArrayAdapter<UserDiscussionResponse> {

		public ResponseAdapter(Context context, List<UserDiscussionResponse> topicList) {
			super(context, R.layout.user_topic_item, topicList);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = viewInflater.inflate(R.layout.user_topic_item, null);
				
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
				holder.timeText = (TextView) convertView.findViewById(R.id.time_text);
				holder.authorText = (TextView) convertView.findViewById(R.id.author_text);
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
			String correctFormat = "%d";
			
			holder.timeText.setVisibility(View.VISIBLE);
			holder.authorText.setVisibility(View.VISIBLE);
			holder.descriptionText.setVisibility(View.VISIBLE);
			
			holder.titleText.setText(Html.fromHtml(response.getTitle()).toString());
			holder.authorText.setText(author.getFirstName() + " " + author.getLastName());
			holder.descriptionText.setText(Html.fromHtml(response.getDescription()));
			holder.timeText.setText(prettyTimeFormatter.format(response.getPostedDate().getTime()));
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

		
	}

}
