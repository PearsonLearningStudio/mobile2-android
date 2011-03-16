package com.ecollege.android.util;

public class CacheConfiguration {
	public boolean bypassFileCache;
	public boolean bypassResultCache;
	public boolean cacheResult;
	public CacheConfiguration() {}
	public CacheConfiguration(boolean bypassFileCache, boolean bypassResultCache, boolean cacheResult) {
		super();
		this.bypassFileCache = bypassFileCache;
		this.bypassResultCache = bypassResultCache;
		this.cacheResult = cacheResult;
	}
}

