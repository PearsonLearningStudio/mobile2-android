package com.ecollege.android;

import java.util.ArrayList;

import roboguice.inject.InjectExtra;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ecollege.android.UserResponseActivity.UserResponseViewAdapter.ExpandableDescriptionHolder;
import com.ecollege.android.activities.ECollegeListActivity;
import com.ecollege.android.adapter.ParentAdapterObserver;
import com.ecollege.android.adapter.ResponseAdapter;
import com.ecollege.android.util.CacheConfiguration;
import com.ecollege.android.view.helpers.ResponseCountViewHelper;
import com.ecollege.api.model.DiscussionResponse;
import com.ecollege.api.model.ResponseCount;
import com.ecollege.api.model.UserDiscussionResponse;
import com.ecollege.api.services.discussions.FetchDiscussionResponsesForResponse;
import com.ecollege.api.services.discussions.PostResponseToResponse;

public class UserResponseActivity extends ECollegeListActivity {

	private static final String USER_RESPONSE_EXTRA = "USER_RESPONSE_EXTRA";
	private static final int VIEW_RESPONSE_REQUEST = 0;
	@InjectExtra(UserTopicActivity.USER_RESPONSE_EXTRA) protected UserDiscussionResponse userResponse;
	
	protected DiscussionResponse response;
	protected ResponseAdapter responseAdapter;
	private ResponseCount responseCount;
	public LayoutInflater viewInflater;
	private UserResponseViewAdapter userResponseAdapter;
	private AlertDialog postDialog;
	private Button cancelPostButton;
	private Button postButton;
	private EditText postResponseText;
	private EditText postTitleText;
	private ExpandableDescriptionHolder expandableDescriptionHolder;
	private Spanned styledDescriptionHtml;
	private boolean descriptionExpanded;
	
	protected View.OnClickListener onDescriptionExpandToggle = new View.OnClickListener() {
		public void onClick(View v) {
			toggleDescription();
		}
	};

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_topic);
		response = userResponse.getResponse();
		responseCount = userResponse.getChildResponseCounts();
		viewInflater = getLayoutInflater();
		
		styledDescriptionHtml = Html.fromHtml(response.getDescription());
		
		loadAndDisplayResponsesForResponse();
		
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
		UserDiscussionResponse selectedResponse = (UserDiscussionResponse)getListAdapter().getItem(position);
		Intent intent = new Intent(this, UserResponseActivity.class);
		intent.putExtra(USER_RESPONSE_EXTRA, selectedResponse);
		startActivityForResult(intent, VIEW_RESPONSE_REQUEST);
	}

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == VIEW_RESPONSE_REQUEST) {
    		reloadAndDisplayResponsesForResponse();
    	}
    }
    
	private void loadAndDisplayResponsesForResponse() {
		setListAdapter(createOrReturnResponseAdapter());
	}
	
	private void reloadAndDisplayResponsesForResponse() {
		responseAdapter = new ResponseAdapter(this, new ArrayList<UserDiscussionResponse>());
		responseAdapter.setLoading(true);
		userResponseAdapter.setResponseAdapter(responseAdapter);
		fetchResponsesForResponse(true);
	}

	private ListAdapter createOrReturnResponseAdapter() {
		if (responseAdapter == null) {
			responseAdapter = new ResponseAdapter(this, new ArrayList<UserDiscussionResponse>());
			if (userResponseAdapter == null) {
				userResponseAdapter = new UserResponseViewAdapter(responseAdapter);
			} else {
				userResponseAdapter.setResponseAdapter(responseAdapter);
			}
			responseAdapter.setLoading(true);
			fetchResponsesForResponse(false);
		}
		return userResponseAdapter;
	}

	protected void showPostDialog() {
		if (postDialog == null) {
			View responseView = viewInflater.inflate(R.layout.post_response, null);
			postDialog = new AlertDialog.Builder(UserResponseActivity.this)
				.setView(responseView)
				.setTitle(R.string.post_a_response)
				.show();
			postDialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			cancelPostButton = (Button) responseView.findViewById(R.id.cancel_button);
			postButton = (Button) responseView.findViewById(R.id.post_button);
			postTitleText = (EditText) responseView.findViewById(R.id.post_title_text);
			postResponseText = (EditText) responseView.findViewById(R.id.post_response_text);
			postResponseText.setHint(getString(R.string.post_a_response_to_s, Html.fromHtml(response.getTitle())));
			cancelPostButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					postDialog.dismiss();
				}
			});
			postButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					postResponse();
				}
			});
		} else {
			postDialog.show();
		}
	}

	
	private void fetchResponsesForResponse(boolean reload) {
		CacheConfiguration cacheConfiguration = new CacheConfiguration();
		cacheConfiguration.bypassFileCache = reload;
		cacheConfiguration.bypassResultCache = reload;
		buildService(new FetchDiscussionResponsesForResponse(userResponse))
			.configureCaching(cacheConfiguration)
			.execute();
	}
	
	public void onServiceCallSuccess(FetchDiscussionResponsesForResponse service) {
		responseAdapter.setNotifyOnChange(false);
		for (UserDiscussionResponse response : service.getResult()) {
			responseAdapter.add(response);
		}
		responseAdapter.setNotifyOnChange(true);
		responseAdapter.setLoading(false);
		responseAdapter.notifyDataSetChanged();
		loadAndDisplayResponsesForResponse();
	}
	
	protected void postResponse() {
		String title = postTitleText.getText().toString();
		String responseText = postResponseText.getText().toString();
		buildService(new PostResponseToResponse(response.getId(), title, responseText))
			.makeModal()
			.execute();
	}
	
	public void onServiceCallSuccess(PostResponseToResponse service) {
		postTitleText.setText("");
		postResponseText.setText("");
		postDialog.hide();
		reloadAndDisplayResponsesForResponse();
		// reach in and change the response counts on the topic
		responseCount.setPersonalResponseCount(responseCount.getPersonalResponseCount() + 1);
		responseCount.setTotalResponseCount(responseCount.getTotalResponseCount() + 1);
		responseCount.setLast24HourResponseCount(responseCount.getLast24HourResponseCount() + 1);
	}

	protected void toggleDescription() {
		if (descriptionExpanded) {
			expandableDescriptionHolder.descriptionText.setText(response.getRawDescription());
			expandableDescriptionHolder.descriptionText.setMaxLines(2);
			expandableDescriptionHolder.textFadeView.setVisibility(View.VISIBLE);
			expandableDescriptionHolder.expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_text));
		} else {
			expandableDescriptionHolder.descriptionText.setText(styledDescriptionHtml);
			expandableDescriptionHolder.descriptionText.setMaxLines(999);
			expandableDescriptionHolder.textFadeView.setVisibility(View.GONE);
			expandableDescriptionHolder.expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_text_upside_down));
		}
		userResponseAdapter.notifyDataSetChanged();
		descriptionExpanded = !descriptionExpanded;
	}
	
	protected class UserResponseViewAdapter extends BaseAdapter {
		
		final int STATIC_VIEWS = 4;
		final long FAKE_ID = 1000000000;
		
		private ResponseAdapter responseAdapter;
		private ParentAdapterObserver adapterObserver;
		public boolean isLoading;

		public UserResponseViewAdapter(ResponseAdapter responseAdapter) {
			assert(responseAdapter != null);
			this.responseAdapter = responseAdapter;
			this.adapterObserver = new ParentAdapterObserver(this);
			responseAdapter.registerDataSetObserver(adapterObserver);
		}
		
		public void setResponseAdapter(ResponseAdapter responseAdapter) {
			this.responseAdapter.unregisterDataSetObserver(adapterObserver);
			this.responseAdapter = responseAdapter;
			this.responseAdapter.registerDataSetObserver(adapterObserver);
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
			case 0 : return getViewForTitle(convertView);
			case 1 : return getViewForParentResponse(convertView);
			case 2 : return getViewForDescription(convertView);
			case 3 : return getViewForPostItem(convertView);
			default : return responseAdapter.getView(position - STATIC_VIEWS, convertView, parent);
			}
		}
		
		public class TitleViewHolder {
			public TextView parentTitleText;
		}
		
		private View getViewForTitle(View convertView) {
			TitleViewHolder holder;
			if (convertView == null) {
				holder = new TitleViewHolder();
				convertView = viewInflater.inflate(R.layout.user_topic_title_view, null);
				holder.parentTitleText = (TextView) convertView.findViewById(R.id.topic_title_text);
				convertView.setTag(holder);
			} else {
				holder = (TitleViewHolder) convertView.getTag();
			}
			holder.parentTitleText.setText(Html.fromHtml(response.getTitle()));
			return convertView;
		}
		
		public class ParentResponseViewHolder {
			public TextView responseTitleText;
			public ImageView responseIcon;
			public TextView userTopicTitleText;
			public TextView totalResponseCountText;
			public TextView unreadResponseCountText;
			public TextView userResponseCountText;
		}
		
		private View getViewForParentResponse(View convertView) {
			ParentResponseViewHolder holder;
			if (convertView == null) {
				holder = new ParentResponseViewHolder();
				convertView = viewInflater.inflate(R.layout.user_topic_on_tertiary, null);
				holder.responseIcon = (ImageView)convertView.findViewById(R.id.icon);
				holder.userTopicTitleText = (TextView)convertView.findViewById(R.id.title_text);
				holder.totalResponseCountText = (TextView)convertView.findViewById(R.id.total_response_count_text);
				holder.unreadResponseCountText = (TextView)convertView.findViewById(R.id.unread_response_count_text);
				holder.userResponseCountText = (TextView)convertView.findViewById(R.id.user_response_count_text);
				View listDividerTexture = convertView.findViewById(R.id.list_divider_texture);
				listDividerTexture.setVisibility(View.VISIBLE);
				
				convertView.setTag(holder);
			} else {
				holder = (ParentResponseViewHolder)convertView.getTag();
			}
			
			String htmlSafeTitle = Html.fromHtml(response.getTitle()).toString();
			holder.userTopicTitleText.setText(htmlSafeTitle);
			
			ResponseCountViewHelper responseCountViewHelper = new ResponseCountViewHelper(
				UserResponseActivity.this,
				holder.responseIcon,
				holder.unreadResponseCountText,
				holder.totalResponseCountText,
				holder.userResponseCountText
			);
			responseCountViewHelper.setResponseCount(responseCount);
			
			return convertView;
		}
		
		public class ExpandableDescriptionHolder {
			public TextView descriptionText;
			public ImageView expandButton;
			public View textFadeView;
		}
		
		private View getViewForDescription(View convertView) {
			if (convertView == null) {
				expandableDescriptionHolder = new ExpandableDescriptionHolder();
				convertView = viewInflater.inflate(R.layout.expandable_description_item, null);
				expandableDescriptionHolder.descriptionText = (TextView) convertView.findViewById(R.id.description_text);
				expandableDescriptionHolder.expandButton = (ImageView) convertView.findViewById(R.id.expand_toggle_button);
				expandableDescriptionHolder.textFadeView = convertView.findViewById(R.id.text_fade_view);
				convertView.setTag(expandableDescriptionHolder);
				expandableDescriptionHolder.expandButton.setOnClickListener(onDescriptionExpandToggle);
				expandableDescriptionHolder.descriptionText.setOnClickListener(onDescriptionExpandToggle);
				descriptionExpanded = false;
				expandableDescriptionHolder.descriptionText.setText(response.getRawDescription());
			} else {
				expandableDescriptionHolder = (ExpandableDescriptionHolder) convertView.getTag();
			}
			return convertView;
		}
		
		public class PostResponseItemViewHolder {
			public Button postResponseButton;
		}

		private View getViewForPostItem(View convertView) {
			final PostResponseItemViewHolder holder;
			if (convertView == null) {
				holder = new PostResponseItemViewHolder();
				convertView = viewInflater.inflate(R.layout.post_response_item, null);
				holder.postResponseButton = (Button) convertView.findViewById(R.id.post_response_button);
				
				convertView.setTag(holder);
			} else {
				holder = (PostResponseItemViewHolder) convertView.getTag();
			}
			
			holder.postResponseButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showPostDialog();
				}
			});
			
			return convertView;
		}


	}
}
