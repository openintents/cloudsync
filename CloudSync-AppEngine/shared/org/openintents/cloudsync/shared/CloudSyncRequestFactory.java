package org.openintents.cloudsync.shared;


import com.google.web.bindery.requestfactory.shared.RequestFactory;


public interface CloudSyncRequestFactory extends RequestFactory {

	CloudSyncRequest taskRequest();

}
