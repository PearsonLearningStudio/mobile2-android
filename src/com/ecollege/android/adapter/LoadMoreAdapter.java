package com.ecollege.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.ecollege.android.R;


public class LoadMoreAdapter extends BaseAdapter {

	public static final long LOAD_MORE_ITEM_ID = Long.MAX_VALUE - 1;//minus one in case max_value is used
	public static final long LOADING_ITEM_ID = Long.MAX_VALUE - 2;//minus one in case max_value is used
	private boolean canLoadMore = true;
	private boolean isLoadingMore = false;
	private ListAdapter baseAdapter;
	private Context context;
	private SimpleObserver baseObserver;
	
	public LoadMoreAdapter(Context context, ListAdapter baseAdapter, boolean canLoadMore) {
		this.baseAdapter = baseAdapter;
		this.canLoadMore = canLoadMore;
		this.context = context;
		baseObserver = new SimpleObserver(this);
		baseAdapter.registerDataSetObserver(baseObserver);
	}
		
	public void setIsLoadingMore(boolean isLoadingMore) {
		this.isLoadingMore = isLoadingMore;
		this.notifyDataSetChanged();
	}
	
	public void update(ListAdapter baseAdapter, boolean canLoadMore) {
		this.isLoadingMore = false;
		this.baseAdapter.unregisterDataSetObserver(baseObserver);
		this.baseAdapter = baseAdapter;
		this.baseAdapter.registerDataSetObserver(baseObserver);
		this.canLoadMore = canLoadMore;
		this.notifyDataSetChanged();
	}
	
	public int getCount() {
		if (canLoadMore || isLoadingMore) return baseAdapter.getCount() + 1;
		return baseAdapter.getCount();
	}

	public Object getItem(int position) {
		if (position < baseAdapter.getCount()) {
			return baseAdapter.getItem(position);
		} else {
			return this;
		}
	}

	public long getItemId(int position) {
		if (position < baseAdapter.getCount()) {
			return baseAdapter.getItemId(position);
		} else {
			if (isLoadingMore) return LOADING_ITEM_ID;
			return LOAD_MORE_ITEM_ID;
		}
	}

	protected int loadMoreViewType() {
		return (baseAdapter.getViewTypeCount() - 1) + 1;
	}
	
	protected int loadingViewType() {
		return (baseAdapter.getViewTypeCount() - 1) + 2;
	}	
	
	public int getItemViewType(int position) {
		if (position < baseAdapter.getCount()) {
			return baseAdapter.getItemViewType(position);
		} else {
			if (isLoadingMore) return loadingViewType();
			return loadMoreViewType();
		}
	}
	
	public View getLoadMoreView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =  ((Activity)context).getLayoutInflater().inflate(R.layout.load_more_item, null);
        }
        return convertView;		
	}
	
	public View getLoadingView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =  ((Activity)context).getLayoutInflater().inflate(R.layout.loading_item, null);
        }
        return convertView;
	}	

	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < baseAdapter.getCount()) {
			return baseAdapter.getView(position,convertView,parent);
		} else {
			if (isLoadingMore) {
				return getLoadingView(position, convertView, parent);
			}
			return getLoadMoreView(position, convertView, parent);
		}
	}

	public int getViewTypeCount() {
		return baseAdapter.getViewTypeCount() + 2;
	}	
	
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
	}

	public boolean hasStableIds() {
		return baseAdapter.hasStableIds();
	}

	public boolean isEmpty() {
		return baseAdapter.isEmpty();
	}

	public boolean areAllItemsEnabled() {
		if (isLoadingMore) return false;
		return baseAdapter.areAllItemsEnabled();
	}

	public boolean isEnabled(int position) {
		if (position < baseAdapter.getCount()) {
			return baseAdapter.isEnabled(position);
		} else {
			if (isLoadingMore) return false;
			return true;
		}
	}
	
	private class SimpleObserver extends DataSetObserver {
		
		private LoadMoreAdapter adapter;
		
		public SimpleObserver(LoadMoreAdapter adapter) {
			this.adapter = adapter;
		}
		
		@Override
		public void onChanged() {
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			adapter.notifyDataSetInvalidated();
		}
		
	}

}
