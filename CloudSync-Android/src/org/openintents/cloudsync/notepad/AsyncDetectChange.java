package org.openintents.cloudsync.notepad;

import java.util.ArrayList;
import java.util.Arrays;

import org.openintents.cloudsync.AsyncSync;
import org.openintents.cloudsync.CloudSyncActivity;
import org.openintents.cloudsync.notepad.NotePad;
import org.openintents.cloudsync.util.ArrayUtil;
import org.openintents.cloudsync.util.Dumper;
import org.openintents.cloudsync.util.JsonUtil;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncDetectChange extends AsyncTask<Void, Void, String[]>{
	
	public static final int NOTE_ARRAY_LOCAL_ID = 0;
	public static final int NOTE_ARRAY_CREATED_DATE = 1;
	public static final int NOTE_ARRAY_MODIFIED_DATE = 2;
	
	public static final int MOD_ARRAY_LUID = 1;
	public static final int MOD_ARRAY_MODIFIED_DATE = 2;
	
	public static final String PACKAGE_NAME = "org.openintents.notepad";
	private long[][] noteArray;
	private long[][] modArray;
	
	public static final String NOTE_AUTHORITY = "org.openintents.notepad";
	public static final Uri NOTE_CONTENT_URI = Uri.parse("content://"+ NOTE_AUTHORITY + "/notes");
	public static final String MOD_AUTHORITY = "org.openintents.cloudsync.contentprovider";
	public static final String MOD_BASE_PATH = "modifys";	
	public static final Uri MOD_CONTENT_URI = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
	
	private static final boolean debug = true;
	private static final String TAG = "AsyncDetectChange";
	
	private static StringBuilder jsonBuilder = new StringBuilder();
	private static StringBuilder jsonDeleteBuilder = new StringBuilder();
	
	private CloudSyncActivity activity;
	
	public AsyncDetectChange(CloudSyncActivity activity) {
		this.activity = activity;
	}

	@Override
	protected String[] doInBackground(Void... arg0) {
		jsonBuilder = new StringBuilder();  // needed so that previous results are not saved in builder.
		jsonDeleteBuilder = new StringBuilder();
		
		noteArray = getNotesArray();
		modArray = getModArray();
		
		
		Dumper.dump("[modArray]", ArrayUtil.getSingleDimenArray(modArray, MOD_ARRAY_LUID));
		Dumper.dump("[NoteArray01]", ArrayUtil.getSingleDimenArray(noteArray,NOTE_ARRAY_LOCAL_ID ));
		if(hasResetHappened()) {
			deleteOISyncData(PACKAGE_NAME);
		}
		
		ArrayList<Long> newNoteList = getNewIdList();
		ArrayList<Long> modNoteList = getModifiedIdList();
		ArrayList<Long> delNoteList = getDelIdList();
		
		addToJson(newNoteList);
		addToJson(modNoteList);
		Ulg.d(""+jsonBuilder);
		addToDelJson(delNoteList);
		
		Ulg.d(packJson());
		Ulg.d(packDelJson());
		//Dumper.updateModTable(activity); // This is should be done after the Result back from the server 
		// is applied to OI Note. Here for only testing purposes.
		
		return new String[]{packJson(),packDelJson()};
	}


	private String packDelJson() {
		String jsonDeleteBuilderString = "";
		if(jsonDeleteBuilder.length()>1) { // -1 necessary to eliminate last ","
			jsonDeleteBuilderString = jsonDeleteBuilder.substring(0, jsonDeleteBuilder.length()-1);
		}
		String jsonDeleteData = "{ \"data\" : [" + jsonDeleteBuilderString + "] }";
		return jsonDeleteData;
	}

	private String packJson() {
		
		String jsonBuilderString = "";
		// This is to remove last comma that is at the end
		if(jsonBuilder.length()>1) {
			jsonBuilderString = jsonBuilder.substring(0, jsonBuilder.length()-1);
		}
		String jsonData = "{ \"data\" : [" + jsonBuilderString + "] }";
		return jsonData;
	}

	private void addToDelJson(ArrayList<Long> delNoteList) {
		
		for (int i = 0; i < delNoteList.size(); i++) {
			jsonDeleteBuilder = JsonUtil.addToJsonDel(delNoteList.get(i),jsonDeleteBuilder);
		}
		
	}

	private ArrayList<Long> getDelIdList() {
		
		ArrayList<Long> delIdList = new ArrayList<Long>();
		
		long[] noteLocalIdList = ArrayUtil.getSingleDimenArray(noteArray, NOTE_ARRAY_LOCAL_ID);
		long[] modLocalIdList = ArrayUtil.getModLocalIdArray(modArray);
		
		Arrays.sort(noteLocalIdList);
		Arrays.sort(modLocalIdList);
		
		for (long modLocalId : modLocalIdList) {
			if(Arrays.binarySearch(noteLocalIdList, modLocalId) > -1){
				// This is not deleted as it is present in noteLocalId and modLocalId
			} else {
				delIdList.add(modLocalId);
			}
		}
		return delIdList;
	}

	private void addToJson(ArrayList<Long> newNoteList) {
		
		String[] PROJECTIONALL = new String[] {
	        NotePad.Notes._ID, // 0
	        NotePad.Notes.TITLE, // 1
	        NotePad.Notes.NOTE,//2
	        NotePad.Notes.CREATED_DATE,
	        NotePad.Notes.MODIFIED_DATE,//4
	        NotePad.Notes.TAGS,
	        NotePad.Notes.ENCRYPTED,//6
	        NotePad.Notes.THEME,
	        NotePad.Notes.SELECTION_START,//8
	        NotePad.Notes.SELECTION_END,
	        NotePad.Notes.SCROLL_POSITION//10
	    };
		
		Cursor cursor = activity.getContentResolver().query(NOTE_CONTENT_URI, PROJECTIONALL, null, null, null);
		cursor.moveToFirst();
		
		for (int i = 0; i < cursor.getCount(); i++) {
			if(newNoteList.contains((Long) cursor.getLong(0))) {
				jsonBuilder = JsonUtil.addToJson(cursor,jsonBuilder);
			}
			cursor.moveToNext();
		}
		cursor.close();
	}

	private ArrayList<Long> getModifiedIdList() {
		
		ArrayList<Long> modifiedIdList = new ArrayList<Long>();
		
		for (int i = 0; i < noteArray.length; i++) {
			for (int j = 0; j < modArray.length; j++) {
				if(noteArray[i][NOTE_ARRAY_LOCAL_ID] == ArrayUtil.getLocalIdFromLUID(modArray[j][MOD_ARRAY_LUID])) {
					if(noteArray[i][NOTE_ARRAY_MODIFIED_DATE] == modArray[j][MOD_ARRAY_MODIFIED_DATE]) {
						// Do nothing. Because it is same as previous syncs note
					} else {
						modifiedIdList.add(noteArray[i][NOTE_ARRAY_LOCAL_ID]);
					}
				}
			}
		}
		Ulg.d(modifiedIdList.toString());
		return modifiedIdList;
	}
	
	/**
	 * It makes the localId array from oi Note and modTable
	 * For modTable first gets localId array from LUID array.
	 * Then if the element of localId array is found in ModId array do nothing
	 * else add to the newIdList which is returned.
	 * @return
	 */

	private ArrayList<Long> getNewIdList() {
		
		ArrayList<Long> newIdList = new ArrayList<Long>();
		
		long[] noteLocalIdArray = ArrayUtil.getSingleDimenArray(noteArray, NOTE_ARRAY_LOCAL_ID);
		long[] modLocalIdArray = ArrayUtil.getModLocalIdArray(modArray);
		Dumper.dump("modLocalIdArray",modLocalIdArray);
		Dumper.dump("noteLocalIdArray", noteLocalIdArray);
		Arrays.sort(noteLocalIdArray);
		Arrays.sort(modLocalIdArray);
		
		for (long noteLocalId : noteLocalIdArray) {
			if(Arrays.binarySearch(modLocalIdArray, noteLocalId) > -1) {
				// Do nothing as note already exist in modTable
			} else {
				newIdList.add(noteLocalId);
			}
		}
		return newIdList;
	}

	private void deleteOISyncData(String packageName) {
		// Make only OI Note to be deleted.
		if (debug) Log.d(TAG,"Going to delete all OI Sync Data with this pckName:-> "+packageName);
		String AUTHORITY = "org.openintents.cloudsync.contentprovider";
		
		String IDMAPS_BASE_PATH = "idmaps";
		 
		Uri IDMAPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + IDMAPS_BASE_PATH);
			
		 
		String MODIFY_BASE_PATH = "modifys";
		 
		Uri MODIFY_CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + MODIFY_BASE_PATH);
		
		 
		String TIME_BASE_PATH = "times";
		 
		Uri TIME_CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + TIME_BASE_PATH);
		
		Uri timeDel = Uri.parse(TIME_CONTENT_URI.toString());
		Uri modDel = Uri.parse(MODIFY_CONTENT_URI.toString());
		Uri idDel = Uri.parse(IDMAPS_CONTENT_URI.toString());
		
		int timeDelret = activity.getContentResolver().delete(timeDel,null, null);
		int modret = activity.getContentResolver().delete(modDel, null, null);
		int idret = activity.getContentResolver().delete(idDel, null, null);
		
		if (debug) Log.d("vincent", "returned vals "+timeDelret+" "+modret+" "+idret);
		
	}
	
	/**
	 * This method checks whether the OI Note was reset or not.
	 * This is done by checking the modLUID which is stored from previous sync.
	 * And noteLUID which is made by multiplying localid*milli_sec_factor+createdDate
	 * If none of the modLUID is found in noteLUID array this means every element from previous
	 * was deleted.
	 * @return false if it was not reset and hence a same value of modLUID and noteLUID was found.
	 */
	private boolean hasResetHappened() {
		
		long[] LUIDArray = ArrayUtil.getSingleDimenArray(modArray,MOD_ARRAY_LUID); 
		
		long[] noteLUIDArray = ArrayUtil.makeLUIDArray(noteArray);
		
		Arrays.sort(LUIDArray);
		Arrays.sort(noteLUIDArray);
		Dumper.dump("[LUiDArray]",LUIDArray );
		Dumper.dump("[noteLuidArr]", noteLUIDArray);
		if(LUIDArray.length == 0) { return false; } // This is because mod table is empty.
		if(noteLUIDArray.length == 0) { return true; } // The oi NOte is empty there is nothing in it.
		for (long modLocalId : LUIDArray) {
			if(Arrays.binarySearch(noteLUIDArray, modLocalId) > -1) {
				return false;
			}
		}
		
		return true;
	}


	private long[][] getModArray() {
		
		Cursor cursor = activity.getContentResolver().query(MOD_CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		long[][] modArray = new long[cursor.getCount()][3];
		for(int i=0;i<cursor.getCount();i++){
			modArray[i][0] = cursor.getLong(0);
			modArray[i][MOD_ARRAY_LUID] = cursor.getLong(1);
			modArray[i][MOD_ARRAY_MODIFIED_DATE] = cursor.getLong(2);
			cursor.moveToNext();
		}
		cursor.close();
		return modArray;
	}


	private long[][] getNotesArray() {
		
		String[] PROJECTION = new String[] {
	        NotePad.Notes._ID, // 0
	        NotePad.Notes.CREATED_DATE,
	        NotePad.Notes.MODIFIED_DATE,
	    };
		
		Cursor cursor = activity.getContentResolver().query(NOTE_CONTENT_URI, PROJECTION, null, null, null);
		cursor.moveToFirst();
		long[][] noteArray = new long[cursor.getCount()][3];
		for(int i=0;i<cursor.getCount();i++) {
			
			noteArray[i][NOTE_ARRAY_LOCAL_ID] = cursor.getLong(0);
			noteArray[i][NOTE_ARRAY_CREATED_DATE] = cursor.getLong(1);
			noteArray[i][NOTE_ARRAY_MODIFIED_DATE] = cursor.getLong(2);
			cursor.moveToNext();
		}
		cursor.close();
		return noteArray;
	}
	
	@Override
	protected void onPostExecute(String[] result) {
		Ulg.d("[ChngNote] inside postexcute of ChngNote");
		
		activity.displayText("Going to fetch data from Server!");
		if (debug) Log.d(TAG, "inside post execute");
		// checked for the case when there is no modification....
		String jsonString = result[0];
		String jsonDeleteString = result[1];
		
		AsyncSync as = new AsyncSync(activity);
    	as.execute(new String[]{jsonString,jsonDeleteString});
    	
		/**
		if(SyncAdapter.isIntentAvailable(activity, "vincent.start")) {
			Intent syncIntent = new Intent("vincent.start");
			syncIntent.putExtra("data", jsonString);
			if (debug) Log.d(TAG,"changed notes delete:-> "+jsonDeleteString);
			syncIntent.putExtra("delete", jsonDeleteString);
			syncIntent.putExtra("package", activity.getPackageName());
			activity.startActivityForResult(syncIntent, SyncActivity.SYNC_REQUEST_CODE);
		}
		*/
		super.onPostExecute(result);
	}

}
