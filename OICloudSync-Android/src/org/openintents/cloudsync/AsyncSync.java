package org.openintents.cloudsync;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.cloudsync.notepad.AsyncApplyResult;
import org.openintents.cloudsync.shared.OICloudSyncRequest;
import org.openintents.cloudsync.shared.OICloudSyncRequestFactory;
import org.openintents.cloudsync.shared.TaskProxy;
import org.openintents.cloudsync.util.Dumper;
import org.openintents.cloudsync.util.NotepadSync;
import org.openintents.cloudsync.util.RecievedData;
import org.openintents.cloudsync.util.SyncUtil;
import org.openintents.cloudsync.util.Ulg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

public class AsyncSync extends AsyncTask<String[], Void, String[] >{
	
	private static final String TAG = "AsyncSync";
	private static final boolean debug = true;
	
	protected static RecievedData[] rdArray=null;
	protected static RecievedData[] mergedRdArray=null;
	protected static long[][] idMapMatrix = null;
	protected static long[][] mergedIdMapMatrix = null;
	static List<TaskProxy> listTasks;
	static List<TaskProxy> mergedListTasks;
	
	private static final int ID_MAP_MATRIX_LOCAL_ID = 0;
	private static final int ID_MAP_MATRIX_GOOGLE_ID = 1;
	CloudSyncActivity activity;
	private static long timeofThisSync ; // This is used to record for the reads from the server
	private static long timeofThisWrite; // This is to record the write to server.
	private static StringBuilder jsonBuilder = new StringBuilder();
	private static StringBuilder jsonDeleteBuilder = new StringBuilder();

	public AsyncSync(Context cloudSyncActivity) {
		this.activity = (CloudSyncActivity) cloudSyncActivity;
	}


	@Override
	protected String[] doInBackground(String[]... params) {
		Ulg.d("--------------------");
		Ulg.d("--------------------");
		jsonBuilder = new StringBuilder();
		jsonDeleteBuilder = new StringBuilder();
		
		//------------------------------------------------------------------------------------
		//                                      Lets get ready with local data
		//------------------------------------------------------------------------------------
		String[] paramInput = params[0];
		String jsonData = paramInput[0];
		String deleteData = paramInput[1]; 
		Ulg.d("vincent", "inside the asyncsync");
		Ulg.d("vincent", "the json data is:-> "+jsonData);
		Ulg.d("vincent", "the delete data is:-> "+deleteData );
		
		    //idMapMatrix is a table which has local ids and GoogleIds and packageName
		idMapMatrix =  getIdMapMatrix();
		rdArray =  getRecievedArray(jsonData); // Will contain received localIds and Json Data from Client
		
		    // These are long values of the local ids that are going to be inserted or deleted.From rdArray
		LinkedList<Long> updateList =  getUpdateList();
		LinkedList<Long> insertList = getInsertList();
		LinkedList<Long> deleteList = getDeleteList(deleteData); // This contains local ids to be deleted
		
		for (int i = 0; i < deleteList.size(); i++) {
			Ulg.d("[asyncsync] Delete list: "+deleteList.get(i));
		}
		    // Time of last sync is taken from table TimeTable. Its the value of previous sync
		long timeOfLastSync = getLastSyncTime();
		
		//------------------------------------------------------------------------------------
		//                                       Now lets fetch the Server Data
		//------------------------------------------------------------------------------------
		    
		    //Going to fetch the current time of server and store it in timeofThis sync
		timeofThisSync = getTimeFromServer();
		
		    // Going to fetch all the data between timeOfLastSync and timeofThisSync
		listTasks = fetchAllFromServer(timeOfLastSync);
		
		//------------------------------------------------------------------------------------
		// If rdArray is empty then just makeJsonArrayForClient. Coz nothing to work on Sever from here
		//------------------------------------------------------------------------------------
		if(rdArray.length == 0 & deleteList.size()==0) {
			if (debug) Log.d(TAG,"nothing from client :-> "+deleteList.size()+" "+rdArray.length);
			updateTimeTable();
			return makeJsonArraysForClient();
		}
		
		//------------------------------------------------------------------------------------
		//                        Some real work of Inserting, Updating and Deleting on Server
		//------------------------------------------------------------------------------------
		    
		    // Insert Newly Created Notes into Server
		insetNewNotes(insertList);
		
		    // Update Changed notes into Server
		updateChangedNotes(updateList);
		
		    // Delete Notes on the server which has been Deleted on local client
		deleteNotes(deleteList);
		
		//------------------------------------------------------------------------------------
		// Make Json Arrays for Return. [0] = {update and insert} , [1] = Delete
		//------------------------------------------------------------------------------------
		updateTimeTable(); // So that previous Sync becomes this syncs time.
		
		//------------------------------------------------------------------------------------
		// Send a C2DM Message to all the devices
		//------------------------------------------------------------------------------------
		if(!(rdArray.length == 0 & deleteList.size()==0)) {
			// This means that something has come from Android for the sync.
			// And hence something for update and insert
			sendC2DM("Update or Insert happened");
		}
		
		return makeJsonArraysForClient();
		
	}


	private void sendC2DM(String message) {
		
		OICloudSyncRequestFactory factory = Util.getRequestFactory(activity, OICloudSyncRequestFactory.class);
		factory.taskRequest().sendC2DM(message).fire(new Receiver<Boolean>() {

			@Override
			public void onSuccess(Boolean arg0) {
				if(!arg0.booleanValue()) {
					if (debug) Log.e(TAG,"The C2DM message was not sent:-> ");
				}
			}
			
		});
	
	}


	private void updateTimeTable() {
		Uri timeUri = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
		ContentValues timeValue = new ContentValues();
		timeValue.put(TimeTable.TIMESTAMP, timeofThisSync);
		timeValue.put(TimeTable.PACKAGE_NAME, NotepadSync.PACKAGE_NAME);
		Uri insTime = activity.getContentResolver().insert(timeUri, timeValue);
		if (debug) Log.d(TAG, "Inserted time uri is "+insTime.toString());
		
	}


	private String[] makeJsonArraysForClient() {
		int il = listTasks.size();
		if(il==0) { 
			if (debug) Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:listTasks) {
				if (debug) Log.d(TAG, "for adding in json id:"+task.getId());
				
				// Json string should be made at last afer the conflict resolution.
				long gId = task.getId();
				long localId = SyncUtil.mapTheMatrix(idMapMatrix, idMapMatrix.length, gId, ID_MAP_MATRIX_GOOGLE_ID, ID_MAP_MATRIX_LOCAL_ID);//getLocalIdFromMapMatrix(gId, idMapMatrix);
				String jsonString = task.getJsonStringData();
				if(task.getTag()=='D' & localId > -1) {
					jsonDeleteBuilder = SyncUtil.addToJson(jsonDeleteBuilder, localId, gId, jsonString);
				} else if (task.getTag()=='D' & localId == -1) {
					// This note from server has tag 'D' but it not found on the local client.
					// Do nothing.
					continue;
				} 
				
				else {
					jsonBuilder = SyncUtil.addToJson(jsonBuilder,localId,gId,jsonString);
				}
				
			}// loop
		}
		
		String jsonBuilderString = "";
		String jsonDeleteBuilderString = "";
		
		if(jsonBuilder.length()>1) {
			jsonBuilderString = jsonBuilder.substring(0, jsonBuilder.length()-1);
		}
		String jsonDataRet = "{ \"data\" : [" + jsonBuilderString + "] }";
		
		if(jsonDeleteBuilder.length()>1) {
			jsonDeleteBuilderString = jsonDeleteBuilder.substring(0, jsonDeleteBuilder.length()-1);
		}
		String jsonDeleteDataRet = "{ \"data\" : [" + jsonDeleteBuilderString + "] }";
		
		if (debug) Log.d(TAG, jsonDataRet);
		if (debug) Log.d(TAG, jsonDeleteDataRet);
		
		Ulg.d("vincent", "the data to be returned is");
		Ulg.d("vincent", "jsondata is:-> "+jsonDataRet);
		Ulg.d("vincent", "delete for the client is:-> "+jsonDeleteDataRet);
		return new String[]{jsonDataRet, jsonDeleteDataRet};
		
	}
	
	/**
	 * This takes a list of localIds and from them gets list of GoogleIds. Using them it gets the Entities 
	 * from the cloud. In those Entities it puts the Tag='D'. And then persists them again to the AppEngine.
	 * With updated time and package Name.
	 * @param deleteList
	 */
	
	private void deleteNotes(LinkedList<Long> deleteList) {
		if (deleteList.size() == 0) {return;}
		List<Long> gIdList = new ArrayList<Long>();
		for (Long localId : deleteList) {
			long googleId = SyncUtil.mapTheMatrix(idMapMatrix, idMapMatrix.length, localId, ID_MAP_MATRIX_LOCAL_ID, ID_MAP_MATRIX_GOOGLE_ID); //getGoogleIdFromMapMatrix(localId, idMapMatrix);
			gIdList.add(googleId);
		}
		
		final List<TaskProxy> list = new ArrayList<TaskProxy>();
		OICloudSyncRequestFactory factory = Util.getRequestFactory(activity,
				OICloudSyncRequestFactory.class);
		OICloudSyncRequest request1 = factory.taskRequest();
		
		request1.queryGoogleIdList(NotepadSync.PACKAGE_NAME, gIdList).fire(
				new Receiver<List<TaskProxy>>() {

					@Override
					public void onSuccess(List<TaskProxy> arg0) {

						list.addAll(arg0);
						if (debug)
							Log.d(TAG,
									"Delete Size of list to be deleted from Engine: "
											+ list.size());

					}

				});

		int il = list.size();
		Ulg.d("[asyncsync] Size of list to be del tagged: "+il);
		// the above returned beans are frozen
		// check the blog
		// http://fascynacja.wordpress.com/tag/autobean-has-been-frozen/
		// for more details

		OICloudSyncRequest request = factory.taskRequest();

		// creating an array.
		TaskProxy[] task = new TaskProxy[list.size()];
		for (int i = 0; i < list.size(); i++) {

			task[i] = request.edit(list.get(i));
			task[i].setTag('D');
			task[i].setAppPackageName(NotepadSync.PACKAGE_NAME);
			task[i].setTimestamp(timeofThisSync);
			request.updateTask(task[i]);

		}
		request.fire();
		// updated the Google App Engine with new Entities.
		
	}


	private void updateChangedNotes(LinkedList<Long> updateList) {
		// Make the List<Long> of AppEngineIds
		if (updateList.size() == 0) {return;}
		List<Long> gIdList = new ArrayList<Long>();
		for (Long localId : updateList) {
			long googleId = SyncUtil.mapTheMatrix(idMapMatrix, idMapMatrix.length, localId, ID_MAP_MATRIX_LOCAL_ID, ID_MAP_MATRIX_GOOGLE_ID); //getGoogleIdFromMapMatrix(localId, idMapMatrix);
			gIdList.add(googleId);
		}

		final List<TaskProxy> list = new ArrayList<TaskProxy>();
		OICloudSyncRequestFactory factory = Util.getRequestFactory(activity,
				OICloudSyncRequestFactory.class);
		OICloudSyncRequest request1 = factory.taskRequest();
		if (debug)
			Log.d(TAG,
					"[updateEntitiesInGoogleAppEngine] going to do the query for modification on AppEngine with this list: "
							+ gIdList.toString());
		// if the list size of updateList is 0 there is nothing to be updated
		// just return
		request1.queryGoogleIdList(NotepadSync.PACKAGE_NAME, gIdList).fire(
				new Receiver<List<TaskProxy>>() {

					@Override
					public void onSuccess(List<TaskProxy> arg0) {

						list.addAll(arg0);
						if (debug)
							Log.d(TAG,
									"[updateEntitiesInGoogleAppEngine] Size of list to be modified returned from Engine: "
											+ list.size());

					}

				});

		int il = list.size();
		
		// the above returned beans are frozen
		// check the blog
		// http://fascynacja.wordpress.com/tag/autobean-has-been-frozen/
		// for more details

		OICloudSyncRequest request = factory.taskRequest();

		// creating an array.
		TaskProxy[] task = new TaskProxy[list.size()];
		for (int i = 0; i < list.size(); i++) {

			task[i] = request.edit(list.get(i));
			long gId = list.get(i).getId();
			long localId = SyncUtil.mapTheMatrix(idMapMatrix, idMapMatrix.length, gId, ID_MAP_MATRIX_GOOGLE_ID, ID_MAP_MATRIX_LOCAL_ID); //getLocalIdFromMapMatrix(gId, idMapMatrix);
			String jsonString = SyncUtil.getJsonFromRDarray(localId, rdArray);
			if (debug)
				Log.d(TAG,
						"[updateEntitiesInGoogleAppEngine] json data for Gid: "
								+ gId + " ; " + jsonString);
			task[i].setJsonStringData(jsonString);
			task[i].setAppPackageName(NotepadSync.PACKAGE_NAME);
			task[i].setTimestamp(timeofThisSync);
			request.updateTask(task[i]);

		}
		request.fire();
		// updated the Google App Engine with new Entities.
		
	}
	
	/**
	 * This method takes a list of localIds and then gets a corresponding JSON data string.
	 * Uploads that JSON data string along with Package Name and TimeStamp of this Sync.
	 * @param insertList This contains a list of localIDs.
	 */


	private void insetNewNotes(LinkedList<Long> insertList) {
		
		//------------------------------------------------------------------------------------
		//             Inserting the newly Created Notes into AppEngine
		//------------------------------------------------------------------------------------
		
		    // Persisting the new Notes on the Server
		OICloudSyncRequestFactory factory = Util.getRequestFactory(activity,
				OICloudSyncRequestFactory.class);
		OICloudSyncRequest request = factory.taskRequest();
		if (insertList.size() == 0) {return;}
		if (insertList.size() > 0) {
			TaskProxy[] task = new TaskProxy[insertList.size()];
			if (debug)
				Log.d(TAG, "[main] made the taskproxy array");
			for (int i = 0; i < insertList.size(); i++) {
				task[i] = request.create(TaskProxy.class);
				String tempJString = SyncUtil.getJsonFromRDarray(
						insertList.get(i), rdArray);
				task[i].setJsonStringData(tempJString);
				task[i].setAppPackageName(NotepadSync.PACKAGE_NAME);
				task[i].setTimestamp(timeofThisSync);
				request.updateTask(task[i]);
			}
			request.fire();
		}

		    // Going to update the IdMapTable with the GoogleIds of New notes which
		    // were persisted on the server.
		        // First fetch all the data with same timeStamp as timeOfThisSync
				// This will return the notes that were just now updated.
		final List<TaskProxy> listExact = new ArrayList<TaskProxy>();
		if (debug)
			Log.d(TAG,
					"going to do the query of newly created tasks to update the idMapTable with exact time: "
							+ timeofThisSync);
		factory.taskRequest()
				.queryExactTimeStamp(NotepadSync.PACKAGE_NAME,
						timeofThisSync).fire(new Receiver<List<TaskProxy>>() {
					@Override
					public void onSuccess(List<TaskProxy> arg0) {
						listExact.addAll(arg0);
						if (debug)Log.d(TAG,"[updateIdMapTable] Size of list"+ listExact.size());

					}
				});

		int il = listExact.size();
		if (il == 0) {
			if (debug)
				Log.d(TAG, "size of taskList returned from server is 0");
		}
		
		    // This saves the GoogleIds of the newly persisted notes into the idMap table. 

		else {
			for (TaskProxy task : listExact) {
				String jsonString = task.getJsonStringData();
				long googleId = task.getId();
				long localId = SyncUtil.getIdFromRDarray(jsonString, rdArray);
				Uri idmapUri = Uri.parse(CloudSyncContentProvider.IDMAPS_CONTENT_URI.toString());
				ContentValues values = new ContentValues();
				values.put(IdMapTable.COLUMN_APPENG_ID, googleId);
				values.put(IdMapTable.COLUMN_LOCAL_ID, localId);
				values.put(IdMapTable.PACKAGE_NAME,
						NotepadSync.PACKAGE_NAME);
				Uri insertedUri = activity.getContentResolver().insert(
						idmapUri, values);
				if (debug)
					Log.d(TAG, insertedUri.toString());

			}
		}
	}
	
	
	private List<TaskProxy> fetchAllFromServer(long timeOfLastSync) {
    	final List<TaskProxy> list = new ArrayList<TaskProxy>();		
		OICloudSyncRequestFactory factory = Util.getRequestFactory(activity, OICloudSyncRequestFactory.class);
		if (debug) Log.d(TAG, "[main] going to do the query of tasks with package name param, and timestamp greate than: "+timeOfLastSync);
		factory.taskRequest().queryTasks(NotepadSync.PACKAGE_NAME,timeOfLastSync).fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				list.addAll(arg0);
				if (debug) Log.d(TAG, "Size of list"+list.size());
			} 
		});
		Ulg.d("TaskProxy List:-> "+list.toString());
		return list;
	}


	private long getLastSyncTime() {

		Uri timeTableUri = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
		Cursor timeTableCursor = activity.getContentResolver().query(timeTableUri, null, TimeTable.PACKAGE_NAME+"=?", new String[] {  NotepadSync.PACKAGE_NAME } , TimeTable.TIMESTAMP);
		
		if(timeTableCursor.getCount() == 0) {
			return 0;
		}
		timeTableCursor.moveToLast(); //  check whether it returns the highest value checked
		long lastSyncTime = timeTableCursor.getLong(1);
		if (debug) Log.d(TAG, "the lastSync time is: "+lastSyncTime);
		timeTableCursor.close();
		return lastSyncTime;
			
	}


	private LinkedList<Long> getDeleteList(String deleteData) {
		JSONObject jdataobj;
		LinkedList<Long> deleteList =  new LinkedList<Long>();
		try {
			jdataobj = new JSONObject(deleteData);
			JSONArray jsonArray = jdataobj.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jobj = jsonArray.getJSONObject(i);
				deleteList.add(jobj.getLong("id"));
			}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		return deleteList;
	}


	private long getTimeFromServer() {

		final List<Long> tempArray = new ArrayList<Long>(); 
		// using this temparray as i dont know how use only long and value out. 
		OICloudSyncRequestFactory timeFactory = Util.getRequestFactory(activity, OICloudSyncRequestFactory.class);
    	timeFactory.taskRequest().getAppEngineTime().fire(new Receiver<Long>() {

			@Override
			public void onSuccess(Long time) {
				if (debug) Log.d(TAG, "long time of this sync:_> "+time.toString());
				tempArray.add(time);
				if (debug) Log.d(TAG,"time from server:-> "+time.toString());
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
		return timeofThisSync;
	}


	private LinkedList<Long> getInsertList() {
		LinkedList<Long> updateList =  new LinkedList<Long>();
		LinkedList<Long> insertList = new LinkedList<Long>();
		boolean flag = false;
		for(int i =0;i<rdArray.length;i++) {
			flag = false;
			for(int j=0;j<idMapMatrix.length;j++) {
				if(rdArray[i].getLocal_id() == idMapMatrix[j][ID_MAP_MATRIX_LOCAL_ID]) {
					flag = true;
					//updateList.add(rdArray[i].getLocal_id());
					if (debug) Log.d(TAG, "updating the list updateList with id: "+rdArray[i].getLocal_id());
					break;
				}
			}
			if(!flag) {
				insertList.add(rdArray[i].getLocal_id());
				if (debug) Log.d(TAG, "inserting the list rdArray with id: "+rdArray[i].getLocal_id());
			}
		}
		return insertList;
	}


	private LinkedList<Long> getUpdateList() {
		LinkedList<Long> updateList =  new LinkedList<Long>();
		LinkedList<Long> insertList = new LinkedList<Long>();
		boolean flag = false;
		if (debug) Log.d(TAG,"message:-> "+rdArray.toString());
		for(int i =0;i<rdArray.length;i++) {
			flag = false;
			for(int j=0;j<idMapMatrix.length;j++) {
				
				if(rdArray[i].getLocal_id() == idMapMatrix[j][ID_MAP_MATRIX_LOCAL_ID]) {
					flag = true;
					
					updateList.add(rdArray[i].getLocal_id());
					if (debug) Log.d(TAG, "updating the list updateList with id: "+rdArray[i].getLocal_id());
					break;
				}
			}
			
			if(!flag) {
				insertList.add(rdArray[i].getLocal_id());
				//if (debug) Log.d(TAG, "inserting the list rdArray with id: "+rdArray[i].getLocal_id());
			}
		}
		
		
		return updateList;
	}
	
	/**
	 * 
	 * @param jsonData is the total data sent by the OI Note
	 * @return It return the array of RecievedData which contains localId and JSON string containing data like 
	 * title etc.
	 */


	private RecievedData[] getRecievedArray(String jsonData) {
		RecievedData[] rdArray=null;
		try {
			JSONObject jdataobj = new JSONObject(jsonData);
			JSONArray jsonArray = jdataobj.getJSONArray("data");
			rdArray = new RecievedData[jsonArray.length()];
			if (debug) Log.d(TAG, "length of rdArray and JsonArray: "+rdArray.length+" : "+jsonArray.length());
			for(int i=0;i<jsonArray.length();i++) {
				JSONObject jobj = jsonArray.getJSONObject(i);
				int localid = jobj.getInt("id");
				String jsonString = jobj.getString("jsonString");
				rdArray[i] = new RecievedData(localid, jsonString);
				if (debug) Log.d(TAG, "local id inside the RD object: "+rdArray[i].getLocal_id()+" "+rdArray[i].getJsonString());
				
			}
			
		} catch (JSONException e) {
			if (debug) Log.d(TAG, "json exception occured",e);
		}
		return rdArray;
	}
	
	/**
	 * 
	 * @return this method returns an array which contains LocalId and Google Id.
	 * 
	 * It is used to know which localIds are already synced and hence have a GoogleId and which
	 * all are new localIds.
	 */


	private long[][] getIdMapMatrix() {
		Uri idmapUri = Uri.parse(CloudSyncContentProvider.IDMAPS_CONTENT_URI.toString());
		Cursor idMapCursor = activity.getContentResolver().query(idmapUri, null, IdMapTable.PACKAGE_NAME + "=?", new String[] {  NotepadSync.PACKAGE_NAME }, null);
		long[][] idMapMatrix = null;
		if (debug) Log.d(TAG,"Going to make an Id map matrix");
		idMapMatrix = new long[idMapCursor.getCount()][2];
		idMapCursor.moveToFirst();
		for(int i=0;i<idMapMatrix.length;i++) {
			idMapMatrix[i][ID_MAP_MATRIX_LOCAL_ID] = Long.valueOf(idMapCursor.getString(1));
			idMapMatrix[i][ID_MAP_MATRIX_GOOGLE_ID] = Long.valueOf(idMapCursor.getString(2));
			idMapCursor.moveToNext();
			if (debug) Log.d(TAG, "idmap local n google"+idMapMatrix[i][0]+" "+idMapMatrix[i][1]);
		}
		if (debug) Log.d(TAG, "length of the idmapmatrix is "+idMapMatrix.length);
		idMapCursor.close();
		
		return idMapMatrix;
		
	}
	

	@Override
	protected void onPostExecute(String[] result) {
		
		Ulg.d("[asyncsync] onPostExec json: "+result[0]);
		Ulg.d("[asyncsync] onPostExec delete: "+result[1]);
		
		activity.displayText("Going to apply results back to OI Note");
		String jsonData = result[0];
		String deleteData = result[1];
		
		AsyncApplyResult aar = new AsyncApplyResult(activity);
		aar.execute(new String[]{jsonData,deleteData});
		/**
		activity.doneSyncing();
		activity.sendResult(result);
		*/
		super.onPostExecute(result);
	}

	
}
