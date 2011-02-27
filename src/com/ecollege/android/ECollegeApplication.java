package com.ecollege.android;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import roboguice.application.RoboApplication;

import android.content.Context;

import com.ecollege.api.ECollegeClient;
import com.google.inject.Binder;
import com.google.inject.Module;

public class ECollegeApplication extends RoboApplication {

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
				//can make a separate module as needed
			}
		});
	}
	

	private int nextProgressDialogTitleId = -1;
    private int nextProgressDialogMsgId = -1;
    
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

    public synchronized void setActiveContext(String className, Context context) {
        WeakReference<Context> ref = new WeakReference<Context>(context);
        this.contextObjects.put(className, ref);
    }

    public synchronized void resetActiveContext(String className) {
        contextObjects.remove(className);
    }
	
	
}
