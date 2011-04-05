package com.ecollege.android.adapter;

public class UberItem<T> {
	
    public enum UberItemType {
    	HEADER,
    	DATA_ITEM,
    	LOAD_MORE_ITEM,
    	LOADING_ITEM,
    	NO_DATA_ITEM,
    	FOOTER
    }
	
	private Object groupId;

	private UberItemType itemType;
	private T dataItem;
	private int dataItemIndex;

	public UberItem(T dataItem, int dataItemIndex) {
		this.itemType = UberItemType.DATA_ITEM;
		this.dataItem = dataItem;
		this.dataItemIndex = dataItemIndex;
	}

	public UberItem(UberItemType itemType) {
		this.itemType = itemType;
	}
	
	public UberItem(UberItemType itemType, Object groupId) {
		this.itemType = itemType;
		this.groupId = groupId;
	}
	
	public UberItemType getItemType() {
		return itemType;
	}
	
	public Object getGroupId() {
		return groupId;
	}
	
	public T getDataItem() {
		return dataItem;
	}
	
	public int getDataItemIndex() {
		return dataItemIndex;
	}
}
