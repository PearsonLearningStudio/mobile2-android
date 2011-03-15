package com.ecollege.android.util;

import java.util.HashMap;

import roboguice.util.Ln;

public class VolatileCacheManager {

	private static final String TAG = VolatileCacheManager.class.getName();
	final protected HashMap<Object, Object> cacheMap = new HashMap<Object, Object>();
	
	public void put(Object key, Object value) {
		// TODO: limit the size of the cache
		// TODO: Add a TTL
		Ln.i(String.format("Cache put key: %s, value: %s", key, value.toString()));
		cacheMap.put(key, value);
	}
	
	public <CachedT> CachedT get(Object key, Class<CachedT> clazz) {
		Object cachedObject = cacheMap.get(key);
		if (null == cachedObject) {
			Ln.i( String.format("Cache miss for key: %s", key));
			return null;
		} else {
			Ln.i( String.format("Cache hit for key: %s", key));
			if (null != clazz) {
				try {
					CachedT castObject = clazz.cast(cachedObject);
					return castObject;
				} catch (ClassCastException cce) {
					Ln.i( String.format("Cache failed to cast object to Class: ", clazz.toString()));
					return null;
				}
			} else {
				return null;
			}
		}
	}
	 public void clear() {
		 cacheMap.clear();
	 }
}
