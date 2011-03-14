package com.ecollege.android;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roboguice.application.RoboApplication;
import roboguice.inject.SharedPreferencesName;
import roboguice.util.Ln;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ecollege.android.errors.ECollegeAlertException;
import com.ecollege.android.errors.ECollegeException;
import com.ecollege.android.errors.ECollegePromptException;
import com.ecollege.android.errors.ECollegePromptRetryException;
import com.ecollege.android.util.FileCacheManager;
import com.ecollege.android.util.VolatileCacheManager;
import com.ecollege.android.view.HeaderView;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.User;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;

public class ECollegeApplication extends RoboApplication implements UncaughtExceptionHandler {
	
	@Inject SharedPreferences prefs;
    final protected VolatileCacheManager volatileCache = new VolatileCacheManager();
	protected Context lastActiveContext;
    private FileCacheManager serviceCache;
    
	
	public ECollegeApplication() {
		super();
		lastActiveContext = this;
		Thread.setDefaultUncaughtExceptionHandler(this); //global error handling on UI thread
	}

	private ECollegeClient client;
	public ECollegeClient getClient() {
		if (client == null) {
			client = new ECollegeClient(getString(R.string.client_string), getString(R.string.client_id));
		}
		return client;
	}
	
	@Override
	protected void addApplicationModules(List<Module> modules) {
		modules.add(new Module() {
			public void configure(Binder binder) {
				 binder.bindConstant().annotatedWith(SharedPreferencesName.class).to("com.ecollege.android");
				//can make a separate module as needed
			}
		});
	}
		
	public void logout() {
		client = null;
		currentUser = null;
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove("grantToken");
		editor.commit(); //change to apply if android 2.2
		Intent i = new Intent(this,MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
		
	public void putObjectInVolatileCache(String keyQualifier, String key, Object object) {
		volatileCache.put(keyQualifier + "-" + key, object);
	}
	
	public <CachedT extends Type> CachedT getObjectOfTypeFromVolatileCache(String keyQualifier, String key, CachedT clazz) {
		CachedT cachedObject = volatileCache.get(keyQualifier + "-" + key, clazz);
		return cachedObject;
	}
	
	public FileCacheManager getServiceCache() {
		if (serviceCache == null) {
			serviceCache = new FileCacheManager(this, 1000 * 60 * 60); //1 hour cache
		}
		return serviceCache;
	}	
	
	private int pendingServiceCalls = 0;
	private User currentUser;
	private int nextProgressDialogTitleId = -1;
    private int nextProgressDialogMsgId = -1;
    
    public int getPendingServiceCalls() {
		return pendingServiceCalls;
	}

	public synchronized void incrementPendingServiceCalls() {
		if (pendingServiceCalls == 0) {
			this.pendingServiceCalls++;
			updateHeaderProgress(true);
		} else {
			this.pendingServiceCalls++;
		}
	}
	
	public synchronized void decrementPendingServiceCalls() {
		if (pendingServiceCalls == 1) {
			this.pendingServiceCalls--;	
			updateHeaderProgress(false);
		} else {
			this.pendingServiceCalls--;
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public int getNextProgressDialogTitleId() {
		return nextProgressDialogTitleId;
	}

	public void setNextProgressDialogTitleId(int nextProgressDialogTitleId) {
		this.nextProgressDialogTitleId = nextProgressDialogTitleId;
	}

	public int getNextProgressDialogMsgId() {
		return nextProgressDialogMsgId;
	}

	public void setNextProgressDialogMsgId(int nextProgressDialogMsgId) {
		this.nextProgressDialogMsgId = nextProgressDialogMsgId;
	}

    private HashMap<String, WeakReference<Context>> contextObjects = new HashMap<String, WeakReference<Context>>();

    public synchronized Context getActiveContext(String className) {
        WeakReference<Context> ref = contextObjects.get(className);
        if (ref == null) {
            return null;
        }

        final Context c = ref.get();
        if (c == null) // If the WeakReference is no longer valid, ensure it is removed.
            contextObjects.remove(className);

        return c;
    }

    public synchronized Context getActiveContext() {
    	return lastActiveContext;
    }
    
    public synchronized void setActiveContext(String className, Context context) {
    	if (!(context instanceof ECollegeApplication)) {
    		lastActiveContext = context;
    	}
        WeakReference<Context> ref = new WeakReference<Context>(context);
        this.contextObjects.put(className, ref);
    }

    public synchronized void resetActiveContext(String className) {
        contextObjects.remove(className);
    }	
	
	private List<WeakReference<HeaderView>> registeredHeaderViews = new ArrayList<WeakReference<HeaderView>>();
	
	public synchronized void updateHeaderProgress(boolean showProgress) {
		for (int i=registeredHeaderViews.size()-1;i>=0;i--) {
			HeaderView hv = registeredHeaderViews.get(i).get();
			if (hv == null) {
				registeredHeaderViews.remove(i);				
			} else {
				hv.setProgressVisibility(showProgress);
			}
		}
	}
	
	public synchronized void registerHeaderView(HeaderView hv) {
		WeakReference<HeaderView> ref = new WeakReference<HeaderView>(hv);
		registeredHeaderViews.add(ref);
		hv.setProgressVisibility(pendingServiceCalls > 0);
	}
	
	public synchronized void unregisterHeaderView(HeaderView hv) {
		for (int i=registeredHeaderViews.size()-1;i>=0;i--) {
			if (registeredHeaderViews.get(i).get() == hv) {
				registeredHeaderViews.remove(i);
			}
		}
	}
	
	public void reportError(Throwable source) {
		ECollegeException ex;
		
		if (source instanceof ECollegeException){
			ex = (ECollegeException)source;
		} else {
			ex = new ECollegePromptException(lastActiveContext, source);
		}
		
		if (ex.getSource() != null) {
			Ln.e(ex.getSource());
		}
		
		if (ex instanceof ECollegeAlertException) {
			Toast.makeText(this,ex.getErrorMessageId(),5000).show();
		} else if (ex instanceof ECollegePromptException) {
			((ECollegePromptException)ex).showErrorDialog();
		} else if (ex instanceof ECollegePromptRetryException) {
			((ECollegePromptRetryException)ex).showErrorDialog();
		}
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		reportError(ex);
	}
	
	
}
