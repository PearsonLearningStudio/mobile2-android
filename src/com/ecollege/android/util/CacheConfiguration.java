package com.ecollege.android.util;

public class CacheConfiguration {
	public boolean bypassFileCache;
	public boolean bypassResultCache;
	public boolean cacheResultInResultCache;
	public boolean cacheResultInFileCache;
	public CacheConfiguration() {}
	public CacheConfiguration(boolean bypassFileCache, boolean bypassResultCache, boolean cacheResultInResultCache, boolean cacheResultInFileCache) {
		super();
		this.bypassFileCache = bypassFileCache;
		this.bypassResultCache = bypassResultCache;
		this.cacheResultInResultCache = cacheResultInResultCache;
		this.cacheResultInFileCache = cacheResultInFileCache;
	}
}

