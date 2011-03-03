package com.ecollege.android;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import roboguice.application.RoboApplication;
import roboguice.inject.SharedPreferencesName;

import com.ecollege.android.tasks.ServiceCallTask;
import com.ecollege.android.view.HeaderView;
import com.ecollege.api.ECollegeClient;
import com.ecollege.api.model.User;
import com.ecollege.api.services.BaseService;
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
				 binder.bindConstant().annotatedWith(SharedPreferencesName.class).to("com.ecollege.android");
				//can make a separate module as needed
			}
		});
	}
	
	public <ServiceT extends BaseService> ServiceCallTask<ServiceT> buildService(ServiceT service) {
		return new ServiceCallTask<ServiceT>(this, service);
	}
		
		
//new ServiceCallTask<FetchDiscussionResponseById>(app,new FetchDiscussionResponseById(userResponseId)) {		
//		
//	}
	
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
	
	
}
