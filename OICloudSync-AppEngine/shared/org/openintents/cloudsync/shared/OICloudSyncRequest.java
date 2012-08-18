package org.openintents.cloudsync.shared;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value = "org.openintents.cloudsync.server.OICloudSyncService", locator = "org.openintents.cloudsync.server.OICloudSyncServiceLocator")
public interface OICloudSyncRequest extends RequestContext {

	Request<TaskProxy> createTask();

	Request<TaskProxy> readTask(Long id);

	Request<TaskProxy> updateTask(TaskProxy task);

	Request<Void> deleteTask(TaskProxy task);

	Request<List<TaskProxy>> queryTasks();

	Request<Long> getAppEngineTime();

	Request<List<TaskProxy>> queryTasks(String packageName);

	Request<List<TaskProxy>> queryTasks(String packageName, Long timestamp);

	Request<List<TaskProxy>> queryExactTimeStamp(String packageName, Long timestamp);

	Request<List<TaskProxy>> queryGoogleIdList(String packageName, List<Long> idList);

	Request<Integer> deleteAll(String packageName);


}
