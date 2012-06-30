package org.openintents.cloudsync.util;

public class RecievedData {
	private long local_id;
	private String jsonString;
	
	public RecievedData(long local_id,String jsonString) {
		this.local_id = local_id;
		this.jsonString = jsonString;
	}
	public long getLocal_id() {
		return local_id;
	}
	public void setLocal_id(long local_id) {
		this.local_id = local_id;
	}
	
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
}
