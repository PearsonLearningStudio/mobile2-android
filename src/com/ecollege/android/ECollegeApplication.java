package com.ecollege.android;

import com.ecollege.api.ECollegeClient;
import com.github.droidfu.DroidFuApplication;

public class ECollegeApplication extends DroidFuApplication {

	private ECollegeClient client;
	public ECollegeClient getClient() {
		if (client == null) {
			client = new ECollegeClient(getString(R.string.client_string), getString(R.string.client_id));
		}
		return client;
	}
}
