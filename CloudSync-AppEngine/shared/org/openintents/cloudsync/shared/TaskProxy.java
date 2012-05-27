package org.openintents.cloudsync.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "org.openintents.cloudsync.server.Task", locator = "org.openintents.cloudsync.server.TaskLocator")
public interface TaskProxy extends ValueProxy {

	String getEmailAddress();

	Long getId();

	String getUserId();

	long getLocallyGeneratedUID();

	long getTimestamp();

	char getTag();

	String getAppPackageName();

	String getJsonStringData();

	void setEmailAddress(String emailAddress);

	void setUserId(String userId);

	void setLocallyGeneratedUID(long locallyGeneratedUID);

	void setTimestamp(long timestamp);

	void setTag(char tag);

	void setAppPackageName(String appPackageName);

	void setJsonStringData(String jsonStringData);

}
