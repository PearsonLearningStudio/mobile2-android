package com.ecollege.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.net.Uri;

import com.ecollege.api.ECollegeHttpResponseCache;
import com.ecollege.api.ECollegeHttpResponseHandler;

public class FileCacheManager implements ECollegeHttpResponseCache {
	
	private final File cacheDir;
	private final long expirationInMillis;	// in ms
	
	/**
	 * FileCacheManager implements a cache system with the application cache directory on android.
	 * 
	 * @param context
	 * @param expiration	A single expiration length (in ms) used for each cache file
	 */
	public FileCacheManager(Context context, long expirationInMillis) {
		cacheDir = context.getCacheDir();
		this.expirationInMillis = expirationInMillis;
	}

	public void addToCache(Uri uri, String value) {

	}

	public void invalidateCacheEntry(File file) {
		synchronized (cacheDir) {
			file.delete();
			file = null;
		}
	}

	public CacheEntry get(String cacheKey) {
		File cacheFile = null;
		long lastModified = Calendar.getInstance().getTimeInMillis();
		synchronized (cacheDir) {
			cacheFile = new File(cacheDir, cacheKey);
			
			if (cacheFile.exists()) {
				lastModified = cacheFile.lastModified();
				long now = Calendar.getInstance().getTimeInMillis();
				
				if (now - lastModified > expirationInMillis) {
					invalidateCacheEntry(cacheFile);
					cacheFile = null;
				}
			}
		}
		
		if (cacheFile != null && cacheFile.exists()){
			GZIPInputStream fin = null;
			try {
				fin = new GZIPInputStream(new FileInputStream(cacheFile));
				String cacheData = ECollegeHttpResponseHandler.streamToString(fin, null);
				return new CacheEntry(cacheData, lastModified);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return null;
	}

	public void put(String cacheKey, String responseContent) {
		synchronized (cacheDir) {
			File cacheFile = new File(cacheDir, cacheKey);
			GZIPOutputStream out = null;
			try {
				out = new GZIPOutputStream(new FileOutputStream(cacheFile));
				out.write(responseContent.getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.finish();
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Integer removeInvalidEntries() {
		File entryFile;
		long lastModified;
		long now = Calendar.getInstance().getTimeInMillis();
		int counter = 0;
		synchronized (cacheDir) {
			Iterator<File> cacheFileList = Arrays.asList(cacheDir.listFiles()).iterator();
			while (cacheFileList.hasNext()) {
				entryFile = cacheFileList.next();
				lastModified = entryFile.lastModified();
				
				if (now - lastModified > expirationInMillis) {
					invalidateCacheEntry(entryFile);
					counter++;
				}
			}
			return counter;
		}
	}

	
}
