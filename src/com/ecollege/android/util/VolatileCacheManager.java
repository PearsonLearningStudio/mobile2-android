package com.ecollege.android.util;

import java.lang.reflect.Type;
import java.util.HashMap;

import android.util.Log;

public class VolatileCacheManager {

	private static final String TAG = VolatileCacheManager.class.getName();
	final protected HashMap<Object, Object> cacheMap = new HashMap<Object, Object>();
	
	public void put(Object key, Object value) {
		// TODO: limit the size of the cache
		// TODO: Add a TTL
		Log.i(TAG, String.format("Cache put key: %s, value: %s", key, value.toString()));
		cacheMap.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <CachedT extends Type> CachedT get(Object key, CachedT clazz) {
		Object cachedObject = cacheMap.get(key);
		if (null == cachedObject) {
			Log.i(TAG, String.format("Cache miss for key: %s", key));
			return null;
		} else {
			Log.i(TAG, String.format("Cache hit for key: %s", key));
			if (null != clazz) {
				try {
					CachedT castObject = (CachedT)cachedObject;
					return castObject;
				} catch (ClassCastException cce) {
					Log.i(TAG, String.format("Cache failed to cast object to Class: ", clazz.toString()));
					return null;
				}
			} else {
				return null;
			}
		}
	}
}
