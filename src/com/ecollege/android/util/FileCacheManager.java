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
import org.apache.commons.io.FileUtils;

import android.content.Context;

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

	public CacheEntry get(String cacheScope, String cacheKey) {
		File cacheFile = null;
		long lastModified = Calendar.getInstance().getTimeInMillis();
		synchronized (cacheDir) {
			File cacheScopeDirectory = findOrCreateDirectoryForScope(cacheScope);
			cacheFile = new File(cacheScopeDirectory, cacheKey);
			
			synchronized (cacheScopeDirectory) {
				if (cacheFile.exists()) {
					lastModified = cacheFile.lastModified();
					long now = Calendar.getInstance().getTimeInMillis();
					
					if (now - lastModified > expirationInMillis) {
						invalidateCacheEntry(cacheFile);
						deleteCacheSubdirectoryIfEmpty(cacheScopeDirectory);
						cacheFile = null;
					}
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

	public void put(String cacheScope, String cacheKey, String responseContent) {
		synchronized (cacheDir) {
			File cacheScopeDirectory = findOrCreateDirectoryForScope(cacheScope);
			synchronized (cacheScopeDirectory) {
				File cacheFile = new File(cacheScopeDirectory, cacheKey);
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
	}

	public void invalidateCacheScope(String cacheScope) {
		synchronized (cacheDir) {
			File cacheScopeDirectory = new File(cacheDir, cacheScope);
			synchronized (cacheScopeDirectory) {
				if (cacheScopeDirectory.exists()) {
					FileUtils.deleteQuietly(cacheScopeDirectory);
					cacheScopeDirectory = null;
				}
			}
		}
	}
	
	public void invalidateCacheKey(String cacheScope, String cacheKey) {
		synchronized (cacheDir) {
			File cacheScopeDirectory = new File(cacheDir, cacheScope);
			synchronized (cacheScopeDirectory) {
				if (cacheScopeDirectory.exists()) {
					File cacheFile = new File(cacheScopeDirectory, cacheKey);
					invalidateCacheEntry(cacheFile);
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

	protected void invalidateCacheEntry(File file) {
		synchronized (cacheDir) {
			File parentDirectory = file.getParentFile();
			file.delete();
			file = null;
			// if the cache entry's parent directory is not the cacheDirectory itself (which was the case in an old implementation)
			if (!cacheDir.getAbsolutePath().equals(parentDirectory.getAbsolutePath())) {
				// delete the parent if empty
				deleteCacheSubdirectoryIfEmpty(parentDirectory);
			}
		}
	}

	protected File findOrCreateDirectoryForScope(String cacheScope) {
		synchronized (cacheDir) {
			File cacheScopeDir = new File(cacheDir, cacheScope);
			if (!cacheScopeDir.exists()) {
				try {
					cacheScopeDir.mkdir();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return cacheScopeDir;
		}
	}

	protected void deleteCacheSubdirectoryIfEmpty(File directory) {
		synchronized (cacheDir) {
			synchronized (directory) {
				if (directory.isDirectory()) {
					String[] paths = directory.list();
					if (paths.length == 0) {
						FileUtils.deleteQuietly(directory);
						directory = null;
					}
				}
			}
		}
	}

	
}
