package com.ecollege.android.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecollege.android.R;


public class HeaderAdapter extends BaseAdapter {

	public static final long STARTING_ITEM_ID = Long.MAX_VALUE - 1000;
	
	protected ListAdapter baseAdapter;
	private Context context;
	private ParentAdapterObserver baseObserver;
	private List<HeaderDataItem> dataItems;
	
	public HeaderAdapter(Context context, ListAdapter baseAdapter) {
		this.baseAdapter = baseAdapter;
		this.context = context;
		calculateHeaders();
		baseObserver = new ParentAdapterObserver(this);
		baseAdapter.registerDataSetObserver(baseObserver);
	}
	
	public void update(ListAdapter baseAdapter) {
		this.baseAdapter.unregisterDataSetObserver(baseObserver);
		this.baseAdapter = baseAdapter;
		this.baseAdapter.registerDataSetObserver(baseObserver);
		this.notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		calculateHeaders();
	}
	
	protected void calculateHeaders() {
		long itemId = STARTING_ITEM_ID;
		dataItems = new ArrayList<HeaderAdapter.HeaderDataItem>();
		String lastLabel = null;
		for (int i=0;i<baseAdapter.getCount();i++) {
			String currentLabel = headerLabelFunction(baseAdapter.getItem(i),i);
			if (lastLabel == null || (currentLabel != null && !currentLabel.equals(lastLabel))) {
				dataItems.add(new HeaderDataItem(currentLabel, itemId));
				itemId++;
			}
			dataItems.add(new HeaderDataItem(i));
			lastLabel = currentLabel;
		}
	}
	
	protected String headerLabelFunction(Object item, int position) {
		//override in subclass
		throw new NotImplementedException();
	}

	public int getCount() {
		return dataItems.size();
	}

	public Object getItem(int position) {
		HeaderDataItem item = dataItems.get(position);
		if (item.getHeaderLabel() != null) {
			return item;
		} else {
			return baseAdapter.getItem(item.getOriginalPosition());
		}

	}

	public long getItemId(int position) {
		HeaderDataItem item = dataItems.get(position);
		if (item.getHeaderLabel() != null) {
			return item.getItemId();
		} else {
			return baseAdapter.getItemId(item.getOriginalPosition());
		}
	}

	protected int loadMoreViewType() {
		return (baseAdapter.getViewTypeCount() - 1) + 1;
	}
	
	protected int loadingViewType() {
		return (baseAdapter.getViewTypeCount() - 1) + 2;
	}	
	
	public int getItemViewType(int position) {
		HeaderDataItem item = dataItems.get(position);
		if (item.getHeaderLabel() != null) {
			return baseAdapter.getViewTypeCount();
		} else {
			return baseAdapter.getItemViewType(item.getOriginalPosition());
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
	
	public View getHeaderView(int position, View convertView, ViewGroup parent, String label) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.list_header_item, null);

            holder = new ViewHolder();
            holder.headerLabelText = (TextView) convertView.findViewById(R.id.header_label_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.headerLabelText.setText(label);
        return convertView;
	}	

	public View getView(int position, View convertView, ViewGroup parent) {
		HeaderDataItem item = dataItems.get(position);
		String headerLabel = item.getHeaderLabel();
		
		if (headerLabel != null) {
			return getHeaderView(position, convertView, parent, headerLabel);
		} else {
			return baseAdapter.getView(item.getOriginalPosition(),convertView,parent);
		}
	}

	public int getViewTypeCount() {
		return baseAdapter.getViewTypeCount() + 1;
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
		return false;
	}

	public boolean isEnabled(int position) {
		HeaderDataItem item = dataItems.get(position);
		if (item.getHeaderLabel() != null) {
			return false;
		} else {
			return baseAdapter.isEnabled(item.getOriginalPosition());
		}
	}
	
    static class ViewHolder {
        TextView headerLabelText;
    }
    
	private class HeaderDataItem {
		private String headerLabel;
		private int originalPosition = -1;
		private long itemId = -1;
		
		public HeaderDataItem(String headerLabel, long itemId) {
			this.headerLabel = headerLabel;
			this.itemId = itemId;
		}

		public HeaderDataItem(int originalIndex) {
			this.originalPosition = originalIndex;
		}
		
		public String getHeaderLabel() {
			return headerLabel;
		}
		
		public int getOriginalPosition() {
			return originalPosition;
		}
		
		public long getItemId() {
			return itemId;
		}
		
	}

}
