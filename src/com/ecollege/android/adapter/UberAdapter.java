package com.ecollege.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecollege.android.R;
import com.ecollege.android.adapter.UberItem.UberItemType;


public class UberAdapter<T> extends BaseAdapter {

	private Context context;
	private boolean isLoading = false;
	private boolean canLoadMore = true;
	private boolean hasHeader;
	private boolean hasFooter;
	private long lastUpdatedAt = -1;
	
	private List<T> dataItems;
	private List<UberItem<T>> uberItems;
	private UberItem<T> loadMoreItem = new UberItem<T>(UberItemType.LOAD_MORE_ITEM);
	private UberItem<T> loadingItem = new UberItem<T>(UberItemType.LOADING_ITEM);
	private UberItem<T> noDataItem = new UberItem<T>(UberItemType.NO_DATA_ITEM);
	private UberItem<T> lastUpdatedItem = new UberItem<T>(UberItemType.LAST_UPDATED_ITEM);
	
	private int specialItemViewType = 0;
	private int dataItemViewType = 1;
	private int headerItemViewType = 2;
	private int footerItemViewType = 3;
	private int lastUpdatedViewType = 4;
	private int itemTypeCount = 2; //+1 if hasHeader, +1 if hasFooter
	
	private SimpleDateFormat lastUpdatedDateFormat;
	private String lastUpdatedWrapperFormat;
	
	public UberAdapter(Context context, boolean hasHeader, boolean hasFooter, boolean canLoadMore) {
		this.canLoadMore = canLoadMore;
		this.context = context;
		this.hasHeader = hasHeader;
		this.hasFooter = hasFooter;
		
		if (hasHeader) itemTypeCount++;
		if (hasFooter) itemTypeCount++;
		if (hasFooter && !hasHeader) {
			footerItemViewType--;
		}
		if (!hasHeader) lastUpdatedViewType--;
		if (!hasFooter) lastUpdatedViewType--;
		
		this.canLoadMore = canLoadMore;
	}
	
	// set to 0 for "Never", anything greater for an actual date
	//  if not called it won't show
	public void setLastUpdatedAt(long lastUpdatedAt) {
		assert lastUpdatedAt >= 0;
		if (this.lastUpdatedAt == -1) {
			itemTypeCount++; //increase item type count
			this.lastUpdatedDateFormat = new SimpleDateFormat(context.getString(R.string.last_updated_date_format));
			this.lastUpdatedWrapperFormat = context.getString(R.string.last_updated_s);
		}
		this.lastUpdatedAt = lastUpdatedAt;
		this.notifyDataSetChanged();
	}
		
	public void beginLoading() {
		this.isLoading = true;
		this.notifyDataSetChanged();
	}
	
	public void hasError() {
		this.isLoading = false;
		this.dataItems = null;
		this.uberItems = null;
		this.notifyDataSetChanged();
	}

	public void updateItems(List<T> dataItems) {
		updateItems(dataItems,canLoadMore);
	}

	public void updateItems(T[] dataItems) {
		updateItems(Arrays.asList(dataItems));
	}
	
	public void updateItems(T[] dataItems, boolean canLoadMore) {
		updateItems(Arrays.asList(dataItems),canLoadMore);
	}
	
	public void updateItems(List<T> dataItems, boolean canLoadMore) {
		assert dataItems != null;
		this.isLoading = false;
		this.canLoadMore = canLoadMore;
		this.dataItems = dataItems;
		calculateGrouping();
		this.notifyDataSetChanged();
	}
	
	protected Object groupIdFunction(T item) {
		//override in subclass
		throw new RuntimeException("groupIdFunction required for " + this.getClass().getSimpleName());
	}
	
	protected void calculateGrouping() {
		assert dataItems != null;
		uberItems = new ArrayList<UberItem<T>>();
		
		Object lastGroupId = null;
		Object currentGroupId = null;
		
		for (int i=0;i<dataItems.size();i++) {
			if (hasHeader || hasFooter) {
				currentGroupId = groupIdFunction(dataItems.get(i));
				
				if (lastGroupId == null && hasHeader) {
					uberItems.add(new UberItem<T>(UberItemType.HEADER,currentGroupId));
				} else if (currentGroupId != null && !currentGroupId.equals(lastGroupId)) {
					if (hasFooter) {
						uberItems.add(new UberItem<T>(UberItemType.FOOTER,currentGroupId));
					}
					if (hasHeader) {
						uberItems.add(new UberItem<T>(UberItemType.HEADER,currentGroupId));
					}
				}
				uberItems.add(new  UberItem<T>(dataItems.get(i),i));
				lastGroupId = currentGroupId;				
			} else {
				uberItems.add(new  UberItem<T>(dataItems.get(i),i));
			}
		}

		if (hasFooter && currentGroupId != null) {
			uberItems.add(new UberItem<T>(UberItemType.FOOTER,currentGroupId));
		}
	}	
	

	public int getCount() {
		if (isLoading) {
			return 1;
		} else {
			if (dataItems == null) {
				return 0; //show nothing while waiting to begin loading
			} else {
				if (dataItems.size() == 0) {
					return 1;  // item that says no items
				} else {
					int count = uberItems.size();
					if (lastUpdatedAt >= 0) count++;
					if (canLoadMore) count++;
					return count;
				}
			}
		}
	}

	public List<T> getDataItems() {
		return null;
	}
	
	public UberItem<T> getItem(int position) {
		if (isLoading) {
			return loadingItem;
		} else {
			if (dataItems == null) {
				return null; //empty list
			} else {
				if (dataItems.size() == 0) {
					return noDataItem; // item that says no items
				} else {
					if (lastUpdatedAt >= 0 && position == 0) {
						return lastUpdatedItem;
					} else if (canLoadMore && position == (getCount() - 1)) {
						return loadMoreItem;
					} else {
						return lastUpdatedAt >= 0 ? uberItems.get(position-1) : uberItems.get(position);
					}
				}
			}
		}
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		UberItem<T> item = getItem(position);
		if (item.getItemType() == UberItemType.LOAD_MORE_ITEM) return specialItemViewType;
		if (item.getItemType() == UberItemType.LOADING_ITEM) return specialItemViewType;
		if (item.getItemType() == UberItemType.NO_DATA_ITEM) return specialItemViewType;
		if (item.getItemType() == UberItemType.HEADER) return headerItemViewType;
		if (item.getItemType() == UberItemType.FOOTER) return footerItemViewType;
		if (item.getItemType() == UberItemType.LAST_UPDATED_ITEM) return lastUpdatedViewType;
		return dataItemViewType;
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

    private static class SpecialItemViewHolder {
        ImageView iconImage;
        ProgressBar loadingIndicator;
        TextView titleText;
    }
    
    private static class HeaderViewHolder {
        TextView headerLabelText;
    }

    protected View getSpecialView(int position, View convertView, ViewGroup parent, UberItem<T> item) {
    	SpecialItemViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.special_item, null);

            holder = new SpecialItemViewHolder();
            holder.iconImage = (ImageView) convertView.findViewById(R.id.icon_image);
            holder.loadingIndicator = (ProgressBar) convertView.findViewById(R.id.loading_indicator);
            holder.titleText = (TextView) convertView.findViewById(R.id.title_text);
            convertView.setTag(holder);
        } else {
            holder = (SpecialItemViewHolder) convertView.getTag();
        }
        
        if (item.getItemType() == UberItemType.NO_DATA_ITEM) {
        	holder.iconImage.setVisibility(View.VISIBLE);
        	holder.loadingIndicator.setVisibility(View.INVISIBLE);
        	holder.iconImage.setImageResource(R.drawable.ic_no_responses);
        	holder.titleText.setText(R.string.li_no_items);
        } else if (item.getItemType() == UberItemType.LOAD_MORE_ITEM) {
        	holder.iconImage.setVisibility(View.INVISIBLE);
        	holder.loadingIndicator.setVisibility(View.INVISIBLE);
        	holder.titleText.setText(R.string.li_load_more);
        } else if (item.getItemType() == UberItemType.LOADING_ITEM) {
        	holder.iconImage.setVisibility(View.INVISIBLE);
        	holder.loadingIndicator.setVisibility(View.VISIBLE);
        	holder.titleText.setText(R.string.li_loading);
        } else {
        	throw new RuntimeException("getSpecialView doesn't support item type " + item.getItemType().toString());
        }
        
        return convertView;
    }
    
	//optionally override in subclass
    protected View getHeaderView(int position, View convertView, ViewGroup parent, Object groupId) {
        HeaderViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.list_header_item, null);

            holder = new HeaderViewHolder();
            holder.headerLabelText = (TextView) convertView.findViewById(R.id.header_label_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        
        holder.headerLabelText.setText(groupId.toString());
        return convertView;
	}	
    

    private static class LastUpdatedViewHolder {
        TextView lastUpdatedText;
    }
    
    protected View getLastUpdatedView(View convertView, ViewGroup parent, UberItem<T> item, long lastUpdatedAt) {
        LastUpdatedViewHolder holder;

        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(R.layout.last_updated_view, null);

            holder = new LastUpdatedViewHolder();
            holder.lastUpdatedText = (TextView) convertView.findViewById(R.id.last_updated_text);
            convertView.setTag(holder);
        } else {
            holder = (LastUpdatedViewHolder) convertView.getTag();
        }
        
        String dateText;
        if (lastUpdatedAt > 0) {
        	dateText = lastUpdatedDateFormat.format(new Date(lastUpdatedAt));
        } else {
        	dateText = context.getString(R.string.never);
        }
        holder.lastUpdatedText.setText(String.format(lastUpdatedWrapperFormat, dateText));
        
        return convertView;
	}

	//must override in subclass if hasFooter
    protected View getFooterView(int position, View convertView, ViewGroup parent, Object groupId) {
		throw new NotImplementedException();
	}


	//must override in subclass
    protected View getDataItemView(View convertView, ViewGroup parent, UberItem<T> item) {
		throw new NotImplementedException();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		UberItem<T> item = getItem(position);
		
		if (item.getItemType() == UberItemType.HEADER) {
			return getHeaderView(position, convertView, parent, item.getGroupId());
		} else if (item.getItemType() == UberItemType.FOOTER) {
			return getFooterView(position, convertView, parent, item.getGroupId());
		} else if (item.getItemType() == UberItemType.LOAD_MORE_ITEM) {
			return getSpecialView(position, convertView, parent, item);
		} else if (item.getItemType() == UberItemType.LOADING_ITEM) {
			return getSpecialView(position, convertView, parent, item);
		} else if (item.getItemType() == UberItemType.NO_DATA_ITEM) {
			return getSpecialView(position, convertView, parent, item);
		} else if (item.getItemType() == UberItemType.LAST_UPDATED_ITEM) {
			return getLastUpdatedView(convertView, parent, item, lastUpdatedAt);
		} else {
			return getDataItemView(convertView, parent, item);
		}
	}

	public int getViewTypeCount() {
		return itemTypeCount;
	}	
	
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return getItem(0) != null;
	}

	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		UberItem<T> item = getItem(position);
		if (item == null) return false;
		if (item == loadMoreItem) return true;
		if (item.getItemType() == UberItemType.DATA_ITEM) return true;
		return false;
	}

}
