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


public class GroupedAdapter extends BaseAdapter {

	public static final long STARTING_ITEM_ID = Long.MAX_VALUE - 1000;
	
	protected ListAdapter baseAdapter;
	private Context context;
	private ParentAdapterObserver baseObserver;
	private List<GroupedDataItem> dataItems;
	private boolean hasFooter;
	private boolean hasHeader;
	
	public GroupedAdapter(Context context, ListAdapter baseAdapter) {
		this(context,baseAdapter,true,false);
	}

	public GroupedAdapter(Context context, ListAdapter baseAdapter, boolean hasHeader, boolean hasFooter) {
		this.hasHeader = hasHeader;
		this.hasFooter = hasFooter;
		this.baseAdapter = baseAdapter;
		this.context = context;
		calculateHeadersAndFooters();
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
		calculateHeadersAndFooters();
	}
	
	protected void calculateHeadersAndFooters() {
		long itemId = STARTING_ITEM_ID;
		dataItems = new ArrayList<GroupedAdapter.GroupedDataItem>();
		Object lastGroupId = null;
		Object currentGroupId = null;
		
		for (int i=0;i<baseAdapter.getCount();i++) {
			currentGroupId = groupIdFunction(baseAdapter.getItem(i),i);
			
			if (lastGroupId == null && hasHeader) {
				dataItems.add(new GroupedDataItem(GroupedDataItemType.HEADER,currentGroupId, itemId));
				itemId++;
			} else if (currentGroupId != null && !currentGroupId.equals(lastGroupId)) {
				if (hasFooter) {
					dataItems.add(new GroupedDataItem(GroupedDataItemType.FOOTER,currentGroupId, itemId));
					itemId++;
				}
				if (hasHeader) {
					dataItems.add(new GroupedDataItem(GroupedDataItemType.HEADER,currentGroupId, itemId));
					itemId++;
				}
			}
			dataItems.add(new GroupedDataItem(i));
			lastGroupId = currentGroupId;
		}

		if (hasFooter && currentGroupId != null) {
			dataItems.add(new GroupedDataItem(GroupedDataItemType.FOOTER,currentGroupId, itemId));
			itemId++;
		}
	}
	
	protected Object groupIdFunction(Object item, int position) {
		//override in subclass
		throw new NotImplementedException();
	}

	public int getCount() {
		return dataItems.size();
	}

	public Object getItem(int position) {
		GroupedDataItem item = dataItems.get(position);
		if (item.getItemType() != GroupedDataItemType.REGULAR_ITEM) {
			return item;
		} else {
			return baseAdapter.getItem(item.getOriginalPosition());
		}

	}

	public long getItemId(int position) {
		GroupedDataItem item = dataItems.get(position);
		if (item.getItemType() != GroupedDataItemType.REGULAR_ITEM) {
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
		GroupedDataItem item = dataItems.get(position);
		if (item.getItemType() != GroupedDataItemType.REGULAR_ITEM) {
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
	
	//optionally override in subclass
	public View getHeaderView(int position, View convertView, ViewGroup parent, Object groupId) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.list_header_item, null);

            holder = new ViewHolder();
            holder.headerLabelText = (TextView) convertView.findViewById(R.id.header_label_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.headerLabelText.setText(groupId.toString());
        return convertView;
	}	

	//must override in subclass
	public View getFooterView(int position, View convertView, ViewGroup parent, Object groupId) {
		throw new NotImplementedException();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		GroupedDataItem item = dataItems.get(position);
		
		if (item.getItemType() == GroupedDataItemType.HEADER) {
			return getHeaderView(position, convertView, parent, item.getGroupId());
		} else if (item.getItemType() == GroupedDataItemType.FOOTER) {
			return getFooterView(position, convertView, parent, item.getGroupId());
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
		GroupedDataItem item = dataItems.get(position);
		if (item.getItemType() != GroupedDataItemType.REGULAR_ITEM) {
			return false;
		} else {
			return baseAdapter.isEnabled(item.getOriginalPosition());
		}
	}
	
    private static class ViewHolder {
        TextView headerLabelText;
    }
    
    private enum GroupedDataItemType {
    	HEADER,
    	REGULAR_ITEM,
    	FOOTER
    }
    
	private class GroupedDataItem {
		private GroupedDataItemType itemType;
		private Object groupId;
		private long itemId = -1;
		
		private int originalPosition = -1;
		
		
		public GroupedDataItem(GroupedDataItemType itemType, Object groupId, long itemId) {
			this.itemType = itemType; //HEADER or FOOTER
			this.groupId = groupId;
			this.itemId = itemId;
		}

		public GroupedDataItem(int originalIndex) {
			this.originalPosition = originalIndex;
			this.itemType = GroupedDataItemType.REGULAR_ITEM;
		}
		
		public GroupedDataItemType getItemType() {
			return itemType;
		}
		
		public Object getGroupId() {
			return groupId;
		}
		
		public int getOriginalPosition() {
			return originalPosition;
		}
		
		public long getItemId() {
			return itemId;
		}
		
	}

}
