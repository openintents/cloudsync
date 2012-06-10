package org.openintents.cloudsync.server;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {

  
  private String emailAddress;//important
  

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;// imp

  //private Boolean done = Boolean.FALSE;// not imp
  //private String name;// not imp
  //private String note;// not imp
  //private Date dueDate;//not important
  private String userId;//important for c2dm updates...
  private long locallyGeneratedUID;
  private long timestamp;
  private char tag;
  private String appPackageName;
  private String jsonStringData;

  public Task() {
  }

 

  public String getEmailAddress() {
    return this.emailAddress;
  }

  public Long getId() {
    return id;
  }

  
  public String getUserId() {
    return userId;
  }  
  
  public long getLocallyGeneratedUID() {
		return locallyGeneratedUID;
	} 

  public long getTimestamp() {
		return timestamp;
	}

  public char getTag() {
		return tag;
	}

  public String getAppPackageName() {
		return appPackageName;
	}

  public String getJsonStringData() {
		return jsonStringData;
	}

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setId(Long id) {
    this.id = id;
  } 
  
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public void setLocallyGeneratedUID(long locallyGeneratedUID) {
		this.locallyGeneratedUID = locallyGeneratedUID;
	}  
  
  public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
  
  public void setTag(char tag) {
		this.tag = tag;
	}
  
  public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
  
  public void setJsonStringData(String jsonStringData) {
		this.jsonStringData = jsonStringData;
	}

  @Override
  public String toString() {
    return null;
  }


}
