package org.openintents.cloudsync;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.cloudsync.shared.CloudSyncRequest;
import org.openintents.cloudsync.shared.CloudSyncRequestFactory;
import org.openintents.cloudsync.shared.TaskProxy;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.google.web.bindery.requestfactory.shared.Violation;


public class AsyncTaskList extends AsyncTask<String, Void, String > {
	private static final String TAG = "CloudSyncActivity";
	private static final int ID_MAP_MATRIX_LOCAL_ID = 0;
	private static final int ID_MAP_MATRIX_GOOGLE_ID = 1;
	CloudSyncActivity activity;
	private static long timeofThisSync ;
	private static StringBuilder jsonBuilder = new StringBuilder();
	private static final boolean debug = true;
	
	public AsyncTaskList(CloudSyncActivity activity) {
		this.activity = activity;
	}

	@Override
	protected String doInBackground(String... params) {
		
		if (debug) Log.d(TAG, "inside the async");
		jsonBuilder = new StringBuilder();
		String jsonData = params[0];
		String deleteData = params[1];
		Uri idmapUri = Uri.parse(CloudSyncContentProvider.IDMAPS_CONTENT_URI.toString());
		Cursor idMapCursor = activity.getContentResolver().query(idmapUri, null, IdMapTable.PACKAGE_NAME + "=?", new String[] {  activity.getCallingPackage() }, null);
		long[][] idMapMatrix = null;
		idMapMatrix =  getIdMapMatrix(idMapCursor);
		if (debug) Log.d(TAG, "length of idMapMatrix: "+idMapMatrix.length);
		
		if (debug) Log.d(TAG, "recieved string is: "+jsonData);
		if (debug) Log.d(TAG,"deleted message is "+deleteData);
		RecievedData[] rdArray=null;
		try {
			// take care when nothing is sent for the sync, then call fetchData and return the value to post execute DONE
			JSONObject jdataobj = new JSONObject(jsonData);
			JSONArray jsonArray = jdataobj.getJSONArray("data");
			
			if(jsonArray.length() == 0) {
				// just fetch the data from the server and make it json string and return
				String jsonStringData = fetchData(idMapMatrix);
				return jsonStringData;
			}
			
			rdArray = new RecievedData[jsonArray.length()];
			if (debug) Log.d(TAG, "length of rdArray: "+rdArray.length);
			for(int i=0;i<jsonArray.length();i++) {
				JSONObject jobj = jsonArray.getJSONObject(i);
				int localid = jobj.getInt("id");
				String jsonString = jobj.getString("jsonString");
				//if (debug) Log.d(TAG, "local id and json in jarray is: "+localid+" "+jsonString);
				rdArray[i] = new RecievedData(localid, jsonString);
				if (debug) Log.d(TAG, "local id inside the RD object: "+rdArray[i].local_id+" "+rdArray[i].jsonString);
				
				
			}
			
		} catch (JSONException e) {
			if (debug) Log.d(TAG, "json exception occured",e);
		}
		
		LinkedList<Long> updateList =  new LinkedList<Long>();
		LinkedList<Long> insertList = new LinkedList<Long>();
		boolean flag = false;
		for(int i =0;i<rdArray.length;i++) {
			flag = false;
			for(int j=0;j<idMapMatrix.length;j++) {
				if (debug) Log.d(TAG, "the rdArray elem and idmapmatrix elem: "+rdArray[i].local_id+" : "+idMapMatrix[j][ID_MAP_MATRIX_LOCAL_ID]);
				if(rdArray[i].local_id == idMapMatrix[j][ID_MAP_MATRIX_LOCAL_ID]) {
					flag = true;
					
					updateList.add(rdArray[i].local_id);
					if (debug) Log.d(TAG, "updating the list updateList with id: "+rdArray[i].local_id);
					break;
				}
			}
			
			if(!flag) {
				insertList.add(rdArray[i].local_id);
				if (debug) Log.d(TAG, "inserting the list rdArray with id: "+rdArray[i].local_id);
			}
		}
		
		final List<Long> tempArray = new ArrayList<Long>(); 
		// using this temparray as i dont know how use only long and value out. 
		CloudSyncRequestFactory timeFactory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
    	timeFactory.taskRequest().getAppEngineTime().fire(new Receiver<Long>() {

			@Override
			public void onSuccess(Long time) {
				if (debug) Log.d(TAG, "[main] inside succes for getting time");
				if (debug) Log.d(TAG, "[main] long time is"+time.toString());
				tempArray.add(time);
				// new calendar inside the appEngine doesnot work
				// use new Date() or System.getmilliseconds
				//if (debug) Log.d(TAG, "time of this sync: "+tempArray.get(0));
			}

			@Override
			public void onFailure(ServerFailure error) {
				if (debug) Log.d(TAG, "[main] it has failed");
				super.onFailure(error);
			}
    	});
		
    	timeofThisSync = tempArray.get(0);
    	
    	deleteNotesFromServer(timeofThisSync,deleteData,idMapMatrix);
    	long timeOfLastSync = getLastSyncTime();
    	
    	final List<TaskProxy> list = new ArrayList<TaskProxy>();		
		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
		if (debug) Log.d(TAG, "[main] going to do the query of tasks with package name param, and timestamp greate than: "+timeOfLastSync);
		factory.taskRequest().queryTasks(activity.getCallingPackage(),timeOfLastSync).fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				
				list.addAll(arg0);
				if (debug) Log.d(TAG, "Size of list"+list.size());
				
			} 
			
			
		});
		
		
		int il = list.size();
		if (debug) Log.d(TAG, "[main] inside Asynctasklist: size of list returned from server"+list.size());
		
		// only for logging purpose <start>
		
		if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				if (debug) Log.d(TAG, "[main] "+task.getId());
				//if (debug) Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				if (debug) Log.d(TAG, "[main] "+task.getTimestamp());
				//if (debug) Log.d(TAG, ""+task.getTag());
				if (debug) Log.d(TAG, "[main] "+task.getAppPackageName());
				if (debug) Log.d(TAG, "[main] "+task.getJsonStringData());
				// Json strig should be made at last afer the conflict resolution.
			}		
			
		}	
		// fetched the notes which are greater than the previous last sync time-stamp.
		
		
		// logging <end>
		CloudSyncRequest request = factory.taskRequest();     
        if (debug) Log.d(TAG,"[main] going to insert new Entities into AppEngine");
        
        if(insertList.size()>0) {
        	TaskProxy[] task = new TaskProxy[insertList.size()]; 
            if (debug) Log.d(TAG,"[main] made the taskproxy array");
            for(int i=0;i<insertList.size();i++) { 
            	task[i] = request.create(TaskProxy.class);
            	String tempJString = getJsonFromRDarray(insertList.get(i),rdArray);
            	task[i].setJsonStringData(tempJString);// if (debug) Log.d(TAG, "the json string going to be inserted: "+tempJString);
            	task[i].setAppPackageName(activity.getCallingPackage());
            	task[i].setTimestamp(timeofThisSync);
            	request.updateTask(task[i]);
            }
            request.fire();
        }
        
        // new notes inserted into the AppEngine. Tested 
        // Now update the IdMapTable with local_ids and GoogleIds
        
        updateIdMapTable(timeofThisSync,rdArray);
		
        // Updating the google App Engine with tasks in updatelist
        
        updateEntitiesInGoogleAppEngine(updateList,idMapMatrix,rdArray);
        
        // Make the jsonString to be sent to the OI Notes
        
        // make jsonArray for the notes that are going to be deleted
        // take the notes form <list> which have TAG 'D'
        if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				if (debug) Log.i(TAG,"tag for del:-> "+task.getTag());
				if( task.getTag()=='D') {
					if (debug) Log.d(TAG,"going to del "+task.getId());
				}
			}
		}
        
        
        // add those notes to delete jsonArray
        // Remove those notes from the <list>
        
        
        // If it is new then -1 is returned else their original id is returned inside jsonData
        if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				if (debug) Log.d(TAG, "for adding in json id:"+task.getId());
				//if (debug) Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				if (debug) Log.d(TAG, ""+task.getTimestamp());
				//if (debug) Log.d(TAG, ""+task.getTag());
				if (debug) Log.d(TAG, ""+task.getAppPackageName());
				if (debug) Log.d(TAG, ""+task.getJsonStringData());
				// Json string should be made at last afer the conflict resolution.
				long gId = task.getId();
				long localId = getLocalIdFromMapMatrix(gId, idMapMatrix);
				String jsonString = task.getJsonStringData();
				addToJson(localId,gId,jsonString);
			}// loop
		}
        
        String jsonBuilderString = "";
		if(jsonBuilder.length()>1) {
			jsonBuilderString = jsonBuilder.substring(0, jsonBuilder.length()-1);
		}
		String jsonDataRet = "{ \"data\" : [" + jsonBuilderString + "] }";
		if (debug) Log.d(TAG, jsonDataRet);
		try {
		    JSONObject mainJobj = new JSONObject(jsonDataRet);
			JSONArray jarray = mainJobj.getJSONArray("data");
		} catch (JSONException e) {
			if (debug) Log.d(TAG, "exception in main json arra",e);
		}
		
		// adding time of this sync in the timeTable
		Uri timeUri = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
		ContentValues timeValue = new ContentValues();
		timeValue.put(TimeTable.TIMESTAMP, timeofThisSync);
		timeValue.put(TimeTable.PACKAGE_NAME, activity.getCallingPackage());
		Uri insTime = activity.getContentResolver().insert(timeUri, timeValue);
		if (debug) Log.d(TAG, "Inserted time uri is "+insTime.toString());
		return jsonDataRet;	
		
        
		//---------------------------------------------------------------------------------------------
        //
        //                                  ------------------------
        //
		//---------------------------------------------------------------------------------------------
		
		
		
		
		
		 //* This is writing stuff into it so deleted
       
        /**
        task[0] = request.create(TaskProxy.class);
    	task[0].setLocallyGeneratedUID(lng+562);    	
    	Calendar cal = Calendar.getInstance();    	
    	task[0].setTimestamp(cal.getTimeInMillis());
    	task[0].setTag('c');
    	task[0].setAppPackageName("com.vettukal.oi");
    	task[0].setJsonStringData("this must be very very long string in randome order");
    	
    	if (debug) Log.d(TAG, "made the first task proxy");
    	task[1] = request.create(TaskProxy.class);
    	
    	
    	task[1].setLocallyGeneratedUID(lng);    	
    	cal = Calendar.getInstance();    	
    	task[1].setTimestamp(cal.getTimeInMillis());
    	task[1].setTag('c');
    	task[1].setAppPackageName("com.vettukal.oi");
    	task[1].setJsonStringData("this must be very very long string in randome order");
    	
    	request.updateTask(task[0]);
    	request.updateTask(task[1]);
    	request.fire();
    	if (debug) Log.d(TAG, "Fire is done");
    	*/
		
        /**
    	CloudSyncRequestFactory factory1 = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
    	factory1.taskRequest().getAppEngineTime().fire(new Receiver<Long>() {

			@Override
			public void onSuccess(Long time) {
				if (debug) Log.d(TAG, "inside succes of Async for getting the time from server");
				if (debug) Log.d(TAG, "long time is"+time.toString());
				// new calendar inside the appEngine doesnot work
				// use new Date() or System.getmilliseconds
				
				
			}

			@Override
			public void onFailure(ServerFailure error) {
				if (debug) Log.d(TAG, "it has failed");
				super.onFailure(error);
			}

			@Override
			public void onViolation(Set<Violation> errors) {
				if (debug) Log.d(TAG, "did some violation");
				super.onViolation(errors);
			}
		});
    	*/
    	
	}

	private void deleteNotesFromServer(long timeofThisSync2, String deleteData, long[][] idMapMatrix) {
//		//convert deleteData into long array of local ids.
//		long[] delIds = getDelIds(deleteData);
//		
//		//get Google ids from IdMapMatrix.
//		List<Long> delGoogleIds = getGoogleIds(delIds,idMapMatrix); 
//		
//		// fetch back all the notes with these delGoogleIds
//		final List<TaskProxy> list = new ArrayList<TaskProxy>();		
//		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
//		CloudSyncRequest request1 = factory.taskRequest();
//		   delGoogleIds.add(Long.valueOf(6));
//		request1.queryGoogleIdList(activity.getCallingPackage(),delGoogleIds).fire(new Receiver<List<TaskProxy>>() {
//
//			@Override
//			public void onSuccess(List<TaskProxy> arg0) {
//				
//				
//				
//				
//				list.addAll(arg0);
//				if (debug) Log.d(TAG, "[Getting del Ids] Size of list to be deleted returned from Engine: "+list.size());
//				
//			} 
//			
//			
//		});
//		
		// Show the list to the user to show if they want to delete the data
		
		
		
		
		// Add the tag 'D' to the fectched back notes from the server.
		   // Remember a new Request has to be made for update. Check update method
		
		
		
		// Done! The Notes are deleted from the server.
	}

	private List<Long> getGoogleIds(long[] delIds, long[][] idMapMatrix) {
		// TODO Auto-generated method stub
		// Maps the local delIds to google ids and returns an array of google ids
		return null;
	}

	private long[] getDelIds(String deleteData) {
		// TODO Auto-generated method stub
		//this method takes the json String and converts into long array of local ids.
		return null;
	}

	private String fetchData(long[][] idMapMatrix) {
		
		if (debug) Log.d(TAG, "inside the fetchData only");
		final List<Long> tempArray = new ArrayList<Long>(); 
		// using this temparray as i dont know how use only long and value out. 
		CloudSyncRequestFactory timeFactory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
    	timeFactory.taskRequest().getAppEngineTime().fire(new Receiver<Long>() {

			@Override
			public void onSuccess(Long time) {
				if (debug) Log.d(TAG, "inside succes of Async for getting the time from server");
				if (debug) Log.d(TAG, "long time is"+time.toString());
				tempArray.add(time);
				// new calendar inside the appEngine doesnot work
				// use new Date() or System.getmilliseconds
				if (debug) Log.d(TAG, "time of this sync: "+tempArray.get(0));
			}

			@Override
			public void onFailure(ServerFailure error) {
				if (debug) Log.d(TAG, "it has failed");
				super.onFailure(error);
			}
    	});
		
    	timeofThisSync = tempArray.get(0);
    	
    	long timeOfLastSync = getLastSyncTime();
    	final List<TaskProxy> list = new ArrayList<TaskProxy>();		
		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
		if (debug) Log.d(TAG, "going to do the query of tasks with package name param, and timestamp greate than: "+timeOfLastSync);
		factory.taskRequest().queryTasks(activity.getCallingPackage(),timeOfLastSync).fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				if (debug) Log.d(TAG, "inside succes of Async");
				
				
				if (debug) Log.d(TAG, "Size of list"+list.size());
				list.addAll(arg0);
				if (debug) Log.d(TAG, "Size of list"+list.size());
				
			} 
			
			
		});
		
		if (debug) Log.d(TAG, "async completed taking the list");
		int il = list.size();
		if (debug) Log.d(TAG, "inside Asynctasklist: size of list returned from server"+list.size());
		
		// only for logging purpose <start>
		
		if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				if (debug) Log.d(TAG, ""+task.getId());
				//if (debug) Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				if (debug) Log.d(TAG, ""+task.getTimestamp());
				//if (debug) Log.d(TAG, ""+task.getTag());
				if (debug) Log.d(TAG, ""+task.getAppPackageName());
				if (debug) Log.d(TAG, ""+task.getJsonStringData());
				
				long gId = task.getId();
				long localId = getLocalIdFromMapMatrix(gId, idMapMatrix);
				String jsonString = task.getJsonStringData();
				addToJson(localId,gId,jsonString);
			}		
			
		}
		
		String jsonBuilderString = "";
		if(jsonBuilder.length()>1) {
			jsonBuilderString = jsonBuilder.substring(0, jsonBuilder.length()-1);
		}
		String jsonDataRet = "{ \"data\" : [" + jsonBuilderString + "] }";
		if (debug) Log.d(TAG, jsonDataRet);
		try {
		    JSONObject mainJobj = new JSONObject(jsonDataRet);
			JSONArray jarray = mainJobj.getJSONArray("data");
		} catch (JSONException e) {
			if (debug) Log.d(TAG, "exception in main json arra",e);
		}
		
		Uri timeUri = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
		ContentValues timeValue = new ContentValues();
		timeValue.put(TimeTable.TIMESTAMP, timeofThisSync);
		timeValue.put(TimeTable.PACKAGE_NAME, activity.getCallingPackage());
		Uri insTime = activity.getContentResolver().insert(timeUri, timeValue);
		if (debug) Log.d(TAG, "Inserted time uri is "+insTime.toString());
		
		return jsonDataRet;	
		
		
	}

	private void addToJson(long localId, long gId, String jsonString) {
		
		String jsonArrElement = " { \"id\": \" "+localId+" \" , " + " \"jsonString\":  "+jsonString+"  , "+  " \"googleId\": \" "+ gId+" \" } " ;
		jsonBuilder.append(jsonArrElement);
		jsonBuilder.append(",");
				                   
	}

	private void updateEntitiesInGoogleAppEngine(LinkedList<Long> updateList,
			long[][] idMapMatrix, RecievedData[] rdArray) {
		
		// Make the List<Long> of AppEngineIds
		List<Long> gIdList = new ArrayList<Long>();
		for(Long localId : updateList) {
			long googleId = getGoogleIdFromMapMatrix(localId,idMapMatrix);
			gIdList.add(googleId);
		}
		
		final List<TaskProxy> list = new ArrayList<TaskProxy>();		
		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
		CloudSyncRequest request1 = factory.taskRequest();    
		if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] going to do the query for modification on AppEngine with this list: "+ gIdList.toString());
		//if the list size of updateList is 0 there is nothing to be updated just return
		request1.queryGoogleIdList(activity.getCallingPackage(),gIdList).fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				
				
				
				
				list.addAll(arg0);
				if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] Size of list to be modified returned from Engine: "+list.size());
				
			} 
			
			
		});
		
		
		int il = list.size();
		if(il==0) { 
			if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] id: "+task.getId());
				//if (debug) Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] TS: "+task.getTimestamp());
				//if (debug) Log.d(TAG, ""+task.getTag());
				if (debug) Log.d(TAG, ""+task.getAppPackageName());
				//if (debug) Log.d(TAG, ""+task.getJsonStringData());
				
				long gId = task.getId();
				long localId = getLocalIdFromMapMatrix(gId, idMapMatrix);
				String jsonString = getJsonFromRDarray(localId, rdArray);
				if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] json data for Gid: "+gId+" ; "+jsonString);
				
			}		
			
		}	
		
		// the above returned beans are frozen
		// check the blog http://fascynacja.wordpress.com/tag/autobean-has-been-frozen/
		// for more details
		
		CloudSyncRequest request = factory.taskRequest();
		
		//creating an array.
		TaskProxy[] task = new TaskProxy[list.size()];
		for(int i=0;i<list.size();i++) {
			
			task[i] = request.edit(list.get(i));
			long gId = list.get(i).getId();
			long localId = getLocalIdFromMapMatrix(gId, idMapMatrix);
			String jsonString = getJsonFromRDarray(localId, rdArray);
			if (debug) Log.d(TAG, "[updateEntitiesInGoogleAppEngine] json data for Gid: "+gId+" ; "+jsonString);
			task[i].setJsonStringData(jsonString);
			task[i].setAppPackageName(activity.getCallingPackage());
			task[i].setTimestamp(timeofThisSync);
			request.updateTask(task[i]);
			
		}
		request.fire();
		// updated the Google App Engine with new Entities.
	}

	private long getLocalIdFromMapMatrix(long gId, long[][] idMapMatrix) {
		
		for(int i=0;i<idMapMatrix.length;i++){
			if(idMapMatrix[i][ID_MAP_MATRIX_GOOGLE_ID]== gId) {
				return idMapMatrix[i][ID_MAP_MATRIX_LOCAL_ID];
			}
		}
			
		return -1;
	}

	private long getGoogleIdFromMapMatrix(Long localId, long[][] idMapMatrix) {
		
		for(int i=0;i<idMapMatrix.length;i++) {
			if(idMapMatrix[i][ID_MAP_MATRIX_LOCAL_ID] == localId) {
				return idMapMatrix[i][ID_MAP_MATRIX_GOOGLE_ID];
			}
		}
		return -1;
	}

	private void updateIdMapTable(long timeofThisSync, RecievedData[] rdArray) {
		
		// First fetch all the data with same timeStamp as timeOfThisSync
		final List<TaskProxy> list = new ArrayList<TaskProxy>();
		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
		if (debug) Log.d(TAG, "going to do the query of newly created tasks to update the idMapTable with exact time: "+timeofThisSync);
		// This returns only entities persisted during this sync right before this calling
		factory.taskRequest().queryExactTimeStamp(activity.getCallingPackage(),timeofThisSync).fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				//if (debug) Log.d(TAG, "inside succes of Async");
				
				
				//if (debug) Log.d(TAG, "Size of list"+list.size());
				list.addAll(arg0);
				if (debug) Log.d(TAG, "[updateIdMapTable] Size of list"+list.size());
				
			} 
			
			
		});
		
		
		int il = list.size();
		if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				// <log start>
				if (debug) Log.d(TAG, "[updateIdMapTable] task id: "+task.getId());
				//if (debug) Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				if (debug) Log.d(TAG, "[updateIdMapTable] task TS: "+task.getTimestamp());
				//if (debug) Log.d(TAG, ""+task.getTag());
				if (debug) Log.d(TAG, "[updateIdMapTable] "+task.getAppPackageName());
				if (debug) Log.d(TAG, "[updateIdMapTable] "+task.getJsonStringData());
				// <log end>
				
				String jsonString = task.getJsonStringData();
				long googleId = task.getId();
				long localId = getIdFromRDarray(jsonString,rdArray);
				
				Uri idmapUri = Uri.parse(CloudSyncContentProvider.IDMAPS_CONTENT_URI.toString());
				ContentValues values = new ContentValues();
				values.put(IdMapTable.COLUMN_APPENG_ID, googleId);
				values.put(IdMapTable.COLUMN_LOCAL_ID, localId);
				values.put(IdMapTable.PACKAGE_NAME, activity.getCallingPackage());
				Uri insertedUri = activity.getContentResolver().insert(idmapUri, values);
				if (debug) Log.d(TAG, insertedUri.toString());
				
			}		
			
		}	
		
	}

	private long getIdFromRDarray(String jsonString, RecievedData[] rdArray) {
	
		
		for(RecievedData rd: rdArray) {
			if(rd.jsonString.equals(jsonString)) {
				return rd.local_id;
			}
		}
		return -1;
	}

	private String getJsonFromRDarray(Long id, RecievedData[] rdArray) {
		
		for(RecievedData rd : rdArray) {
			if(rd.local_id == id ) {
				return rd.jsonString;
				
			}
		}
		return null;
	}

	private long getLastSyncTime() {
		
		Uri timeTableUri = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
		Cursor timeTableCursor = activity.getContentResolver().query(timeTableUri, null, TimeTable.PACKAGE_NAME+"=?", new String[] {  activity.getCallingPackage() } , TimeTable.TIMESTAMP);
		
		if(timeTableCursor.getCount() == 0) {
			return 0;
		}
		timeTableCursor.moveToLast(); //  check whether it returns the highest value checked
		if (debug) Log.d(TAG, "the lastSync time is: "+timeTableCursor.getLong(1));
		return timeTableCursor.getLong(1);
	}

	private long[][] getIdMapMatrix(Cursor idMapCursor) {
		if (debug) Log.d(TAG,"Going to make an Id map matrix");
		int tempi = idMapCursor.getCount();
		long[][] idMapMatrix = new long[idMapCursor.getCount()][2];
		idMapCursor.moveToFirst();
		for(int i=0;i<idMapMatrix.length;i++) {
			//if (debug) Log.d(TAG,"idmapcursor1: "+idMapCursor.getShort(columnIndex))
			idMapMatrix[i][0] = Long.valueOf(idMapCursor.getString(1));
			idMapMatrix[i][1] = Long.valueOf(idMapCursor.getString(2));
			idMapCursor.moveToNext();
			if (debug) Log.d(TAG, ""+idMapMatrix[i][0]+" "+idMapMatrix[i][1]);
		}
		if (debug) Log.d(TAG, "length of the idmapmatrix is "+idMapMatrix.length);
		return idMapMatrix;
	}

	@Override
	protected void onPostExecute(String result) {
		if (debug) Log.d(TAG, "inside the post execute");
		activity.doneSyncing();
		activity.sendResult(result);
		
		super.onPostExecute(result);
		
	}
	
	/**
	@Override
	protected void onPostExecute(String[][] serverArr) {
		if (debug) Log.d(TAG, "onPostExecute() of asynctasklist" );
		activity.setServerArr(serverArr);
	}
	*/
	
	class RecievedData {
		long local_id;
		String jsonString;
		RecievedData(long local_id,String jsonString) {
			this.local_id = local_id;
			this.jsonString = jsonString;
		}
	}

	
}
