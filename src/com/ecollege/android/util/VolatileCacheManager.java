package com.ecollege.android.util;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import roboguice.util.Ln;

public class VolatileCacheManager {
	
	private static final int INITIAL_CACHE_CAPACITY = 255;

	final protected ConcurrentHashMap<Object, SoftReference<Object>> cacheMap = new ConcurrentHashMap<Object, SoftReference<Object>>(INITIAL_CACHE_CAPACITY);
	
	public void put(Object key, Object value) {
		// TODO: limit the size of the cache more manually than with SoftReference?
		// TODO: Add a TTL
		Ln.i(String.format("Cache put key: %s, value: %s", key, value.toString()));
		cacheMap.put(key, new SoftReference<Object>(value));
	}
	
	public <CachedT> CachedT get(Object key, Class<CachedT> clazz) {
		SoftReference<Object> ref = cacheMap.get(key);
		Object cachedObject = null;
		if (null != ref) {
			cachedObject = ref.get();
		}
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
