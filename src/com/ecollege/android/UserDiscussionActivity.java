package com.ecollege.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.ResponseAdapter;
import com.ecollege.android.adapter.UberItem;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.model.UserDiscussionTopic;
import com.ecollege.api.services.discussions.FetchDiscussionResponsesForResponse;
import com.ecollege.api.services.discussions.FetchDiscussionResponsesForTopic;
import com.ecollege.api.services.discussions.PostResponseToResponse;
import com.ecollege.api.services.discussions.PostResponseToTopic;

public abstract class UserDiscussionActivity extends ECollegeListActivity {

	private static final int VIEW_RESPONSE_REQUEST = 0;
	
	protected ResponseAdapter responseAdapter;
	protected ResponseCount responseCount;
	protected AlertDialog postDialog;
	protected Button cancelPostButton;
	protected Button postButton;
	protected EditText postResponseText;
	protected EditText postTitleText;
	protected boolean descriptionExpanded;
	protected Spanned styledDescriptionHtml;
	protected Bundle lastSavedInstanceState;
	
	protected View.OnClickListener onDescriptionExpandToggle = new View.OnClickListener() {
		public void onClick(View v) {
			toggleDescription();
		}
	};
	
	protected static class HeaderViewHolder {
		public TextView parentTitleText;
		public ImageView topicIcon;
		public TextView userTopicTitleText;
		public TextView totalResponseCountText;
		public TextView unreadResponseCountText;
		public TextView userResponseCountText;
		public TextView descriptionText;
		public ImageView expandButton;
		public View textFadeView;
		public Button postResponseButton;
	}
	
	protected HeaderViewHolder headerViewHolder;
	
	protected UserDiscussionTopic getUserTopic() {
		return null;
	}
	
	protected UserDiscussionResponse getUserResponse() {
		return null;
	}
	
	protected void setupView() {
		View headerView = getLayoutInflater().inflate(R.layout.discussion_header,null);
		HeaderViewHolder holder = new HeaderViewHolder();
		holder.parentTitleText = (TextView) headerView.findViewById(R.id.parent_title_text);
		holder.topicIcon = (ImageView)headerView.findViewById(R.id.icon);
		holder.userTopicTitleText = (TextView)headerView.findViewById(R.id.title_text);
		holder.totalResponseCountText = (TextView)headerView.findViewById(R.id.total_response_count_text);
		holder.unreadResponseCountText = (TextView)headerView.findViewById(R.id.unread_response_count_text);
		holder.userResponseCountText = (TextView)headerView.findViewById(R.id.user_response_count_text);
		holder.descriptionText = (TextView) headerView.findViewById(R.id.header_description_text);
		holder.expandButton = (ImageView) headerView.findViewById(R.id.expand_toggle_button);
		holder.textFadeView = headerView.findViewById(R.id.text_fade_view);
		holder.expandButton.setOnClickListener(onDescriptionExpandToggle);
		holder.descriptionText.setOnClickListener(onDescriptionExpandToggle);
		holder.postResponseButton = (Button) headerView.findViewById(R.id.post_response_button);			
		holder.postResponseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPostDialog();
			}
		});
		headerViewHolder = holder;
		getListView().addHeaderView(headerView);
		descriptionExpanded = false;
		responseAdapter = new ResponseAdapter(this);
		setListAdapter(responseAdapter);
	}
	
	protected void restorePostIfSaved(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			boolean wasEditingPost = savedInstanceState.getBoolean("editingPost");
			if (wasEditingPost) {
				showPostDialog();
				String postSubject = savedInstanceState.getString("postSubject");
				String postBody = savedInstanceState.getString("postBody");
				postTitleText.setText(postSubject);
				postResponseText.setText(postBody);
			}
		}
	}
	
	@Override protected void onSaveInstanceState(Bundle outState) {
		if (postDialog != null && postDialog.isShowing()) {
			outState.putBoolean("editingPost", true);
			outState.putString("postSubject", postTitleText.getText().toString());
			outState.putString("postBody", postResponseText.getText().toString());
		}
		super.onSaveInstanceState(outState);
	}

	@Override protected void onPause() {
		super.onPause();
		if (postDialog != null) {
			postDialog.dismiss();
			postDialog = null;
		}
	}

	@Override protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		@SuppressWarnings("unchecked")
		UberItem<UserDiscussionResponse> item = (UberItem<UserDiscussionResponse>)l.getItemAtPosition(position);
		Intent intent = new Intent(this, UserResponseActivity.class);
		intent.putExtra(UserResponseActivity.USER_RESPONSE_EXTRA, item.getDataItem());
		startActivityForResult(intent, VIEW_RESPONSE_REQUEST);
	}

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == VIEW_RESPONSE_REQUEST) {
    		fetchResponses(true);
    	}
    }

	protected void showPostDialog() {
		if (postDialog == null) {
			View responseView = getLayoutInflater().inflate(R.layout.post_response, null);
			postDialog = new AlertDialog.Builder(UserDiscussionActivity.this)
				.setView(responseView)
				.setTitle(R.string.post_a_response)
				.show();
			postDialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			cancelPostButton = (Button) responseView.findViewById(R.id.cancel_button);
			postButton = (Button) responseView.findViewById(R.id.post_button);
			postTitleText = (EditText) responseView.findViewById(R.id.post_title_text);
			postResponseText = (EditText) responseView.findViewById(R.id.post_response_text);
			
			String title = getUserTopic() != null ? getUserTopic().getTopic().getTitle() : getUserResponse().getResponse().getTitle();
			postResponseText.setHint(getString(R.string.post_a_response_to_s, Html.fromHtml(title)));
			cancelPostButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					postDialog.dismiss();
				}
			});
			postButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onPostResponse();
				}
			});
		} else {
			postDialog.show();
		}
	}

	protected void onPostResponse() {
		String title = postTitleText.getText().toString();
		String response = postResponseText.getText().toString();
		
		if (getUserTopic() != null) {
			buildService(new PostResponseToTopic(getUserTopic().getTopic().getId(), title, response))
				.makeModal()
				.execute();
		} else {
			buildService(new PostResponseToResponse(getUserResponse().getResponse().getId(), title, response))
			.makeModal()
			.execute();
		}
	}
	
	protected void fetchResponses(boolean reload) {
		responseAdapter.beginLoading();
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		
		if (getUserTopic() != null) {
			buildService(new FetchDiscussionResponsesForTopic(getUserTopic()))
			.configureCaching(cacheConfiguration)
			.execute();
		} else {
			buildService(new FetchDiscussionResponsesForResponse(getUserResponse()))
			.configureCaching(cacheConfiguration)
			.execute();
		}
	}

	public void onServiceCallSuccess(FetchDiscussionResponsesForTopic service) {
		responseAdapter.setLastUpdatedAt(service.getCompletedAt());
		responseAdapter.updateItems(service.getResult());
	}
	
	public void onServiceCallException(FetchDiscussionResponsesForTopic service, Exception ex) {
		responseAdapter.hasError();
	}

	public void onServiceCallSuccess(FetchDiscussionResponsesForResponse service) {
		responseAdapter.setLastUpdatedAt(service.getCompletedAt());
		responseAdapter.updateItems(service.getResult());
	}
	
	public void onServiceCallException(FetchDiscussionResponsesForResponse service, Exception ex) {
		responseAdapter.hasError();
	}
	
	public void onServiceCallSuccess(PostResponseToTopic service) {
		onPostResponseSuccess();
	}
	
	public void onServiceCallSuccess(PostResponseToResponse service) {
		onPostResponseSuccess();
	}
	
	protected void onPostResponseSuccess() {
		postTitleText.setText("");
		postResponseText.setText("");
		postDialog.hide();
		fetchResponses(true);
		// reach in and change the response counts on the topic
		responseCount.setPersonalResponseCount(responseCount.getPersonalResponseCount() + 1);
		responseCount.setTotalResponseCount(responseCount.getTotalResponseCount() + 1);
		responseCount.setLast24HourResponseCount(responseCount.getLast24HourResponseCount() + 1);
	}

	protected void refreshDescriptionView() {
		if (descriptionExpanded) {
			headerViewHolder.descriptionText.setText(styledDescriptionHtml);
			headerViewHolder.descriptionText.setMaxLines(999);
			headerViewHolder.textFadeView.setVisibility(View.GONE);
			headerViewHolder.expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_text_upside_down));
		} else {
			headerViewHolder.descriptionText.setText(getUserTopic() != null ? getUserTopic().getTopic().getRawDescription() : getUserResponse().getResponse().getRawDescription());
			headerViewHolder.descriptionText.setMaxLines(4);
			headerViewHolder.textFadeView.setVisibility(View.VISIBLE);
			headerViewHolder.expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_text));
		}
	}
	
	protected void toggleDescription() {
		descriptionExpanded = !descriptionExpanded;
		refreshDescriptionView();
	}

}
