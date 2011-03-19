package com.ecollege.android.adapter;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;

public class ParentAdapterObserver extends DataSetObserver {
	private BaseAdapter adapter;
	
	public ParentAdapterObserver(BaseAdapter adapter) {
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
