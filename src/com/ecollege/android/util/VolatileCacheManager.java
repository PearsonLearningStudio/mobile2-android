package com.ecollege.android.util;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import roboguice.util.Ln;

public class VolatileCacheManager {
	
	private static final int INITIAL_CACHE_CAPACITY = 255;
	
	protected class CacheEntry {
		public Object value;
		public long cachedAt;
		public CacheEntry(Object value, long cachedAt) {
			super();
			this.value = value;
			this.cachedAt = cachedAt;
		}
		public Boolean valueIsOlderThanMillis(long expirationInMillis) {
			if (System.currentTimeMillis() - cachedAt <= expirationInMillis) {
				return false;
			}
			return true;
		}
	}

	final protected ConcurrentHashMap<Object, SoftReference<CacheEntry>> cacheMap = new ConcurrentHashMap<Object, SoftReference<CacheEntry>>(INITIAL_CACHE_CAPACITY);
	
	protected final long expirationInMillis;
	
	public VolatileCacheManager(long expirationInMillis) {
		super();
		this.expirationInMillis = expirationInMillis;
	}

	public void put(Object key, Object value) {
		// TODO: limit the size of the cache more manually than with SoftReference?
		// TODO: Add a TTL
		Ln.i(String.format("Cache put key: %s, value: %s", key, value.toString()));
		cacheMap.put(key, new SoftReference<CacheEntry>(new CacheEntry(value, System.currentTimeMillis())));
	}
	
	public <CachedT> CachedT get(Object key, Class<CachedT> clazz) {
		SoftReference<CacheEntry> ref = cacheMap.get(key);
		CacheEntry cacheEntry = null;
		Object cachedObject = null;
		if (null != ref) {
			cacheEntry = ref.get();
			if (cacheEntry != null) {
				if (cacheEntry.valueIsOlderThanMillis(expirationInMillis)) {
					cacheMap.remove(key);
					Ln.i(String.format("Cache key was expired: %s", key));
				} else {
					cachedObject = cacheEntry.value;
				}
			}
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
