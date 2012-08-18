package org.openintents.cloudsync.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.cloudsync.Util;
import org.openintents.cloudsync.notepad.NotePad;
import org.openintents.cloudsync.notepad.Ulg;
import org.openintents.cloudsync.notepad.NotePad.Notes;
import org.openintents.cloudsync.util.NotepadSync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ApplyResult {
	
	static String tag = "vincent";
	static String TAG = "AsyncApplyResult";
	private static final boolean debug = true;
	//CloudSyncActivity activity;
	Context context;
	static String[] PROJECTIONALL = new String[] {
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
	static final int MOD_MODIFIED = 2;
	static final int MOD_LOCALID = 1;
	static final int NOTE_MODIFIED = 1;

	public ApplyResult(Context context) {
		this.context = context;
	}

	public void applyExecute(String[]... params) {
		
		if (debug) Log.v(TAG, "do in back of apply result");
		String[] paramres = params[0];
		String jsonData = paramres[0];
		String deleteData = paramres[1];
		Ulg.d("vincent", "[applyres] inside the applyResult");
		Ulg.d("[applyres] Apply json data is: "+jsonData);
		Ulg.d("[applyres] apply delete data is "+deleteData);
		try {
			JSONObject jsonMainObj = new JSONObject(jsonData);
			JSONArray jsonArray = jsonMainObj.getJSONArray("data");
			if(jsonArray.length() == 0){
				// this means nothing is to be updated back in OI Notes
				if (debug) Log.d(TAG,"No notes to apply back:-> "+"");
				//return null;
			}
			
			for(int i=0;i<jsonArray.length();i++){
				JSONObject jobj = jsonArray.getJSONObject(i);
				
				int localId = jobj.getInt("id");
				if(localId==-1) {
					// No id is there which means it needs to be inserted and value updated to IdMapTable
					insertNote(jobj);
				}
				else {
					// Note is already present and we also have the Id. Simple updation needs to be done.
					JSONObject noteobj = jobj.getJSONObject("jsonString");
					
					Uri notes = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
					notes = Uri.withAppendedPath(notes, String.valueOf(localId));
					
					ContentValues values = new ContentValues();
					values.put(Notes.TITLE, noteobj.getString("title").trim());
					values.put(Notes.NOTE, noteobj.getString("note").trim());
					values.put(Notes.CREATED_DATE, noteobj.getLong("created_date") );
					values.put(Notes.MODIFIED_DATE, noteobj.getLong("modified_date"));
					
					if(!(noteobj.getString("tags").trim().equals("null"))) {
						values.put(Notes.TAGS, noteobj.getString("tags"));
					}
					
					if(!(noteobj.getString("encrypted").trim().equals("null"))) {
						values.put(Notes.ENCRYPTED, noteobj.getLong("encrypted"));
					}
					
					if(!(noteobj.getString("theme").trim().equals("null"))) {
						values.put(Notes.THEME, noteobj.getString("theme"));
					}
					
					
					values.put(Notes.SELECTION_START, noteobj.getLong("selection_start"));
					values.put(Notes.SELECTION_END, noteobj.getLong("selection_end"));
					values.put(Notes.SCROLL_POSITION, noteobj.getDouble("scroll_position"));
					
					int returnInt = context.getContentResolver().update(notes, values, null, null);
					if (debug) Log.d(TAG, "after updating "+noteobj.getString("title")+" return values is: "+returnInt);
					
				}
			}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		if (debug) Log.d(TAG,"going to delte notes:-> "+"");
		deleteNotes(deleteData);
		if (debug) Log.d(TAG,"deleted notes:-> "+"");
		refreshModTable();
		applyPostExecute(null);
		
	}
	
private void refreshModTable() {
		
		//
		
		long[][] modMatrix = getmodMatrix();
		long[][] noteMatrix = getNoteMatrix();
		if(noteMatrix.length == 0 | modMatrix.length == 0) { return; }
		
		String MOD_AUTHORITY = "org.openintents.cloudsync.contentprovider";
		String MOD_BASE_PATH = "modifys";
        
		Ulg.d("-------------------------");
		for (int i = 0; i < noteMatrix.length; i++) {
			Ulg.d("[applyRes] notematrix: "+noteMatrix[i][0]+" "+noteMatrix[i][1]);
		}
		
		for (int i = 0; i < modMatrix.length; i++) {
			Ulg.d("[applyRes] Modmatrix id= "+modMatrix[i][0]+" : "+modMatrix[i][MOD_LOCALID]+" "+modMatrix[i][MOD_MODIFIED]);
		}
		Ulg.d("------------------------");
		Ulg.d("------------------------");
		for (int i = 0; i < modMatrix.length; i++) {
			Uri modUri = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
			boolean notfound = true;
			
			for (int j = 0; j < noteMatrix.length; j++) {
				long modlocalid = modMatrix[i][MOD_LOCALID];
				long notelocalid = noteMatrix[j][0];
				long modModi = modMatrix[i][MOD_MODIFIED];
				long notemodi = noteMatrix[j][1];
				if(modMatrix[i][MOD_LOCALID] == noteMatrix[j][0]) {
					notfound = false;
					if(modMatrix[i][MOD_MODIFIED] == noteMatrix[j][1]) {
						// its ok!
					} else {
						if (debug) Log.e(TAG,"This should not happen!!!:-> "+modMatrix[i][MOD_MODIFIED]);
					}
				}
			}
			if(notfound) {
				// this element of modMatrix is not there in OI Note and hence should be deleted
				modUri = Uri.withAppendedPath(modUri, Long.toString(modMatrix[i][0]));
				int dels = context.getContentResolver().delete(modUri, null, null);
				if (debug) Log.d(TAG,"deleted the string with id:-> "+Long.toString(modMatrix[i][0]));
			}
			
		}
		/**
		 * deleting the modTable elements which do not exist in note table can be easily done by
		 * first taking all the ids from NOte table into an array. Sort it, and do binary search of each
		 * modTable element in that array. if found ok else just delete it.
		 */
		
		
	}

	private long[][] getNoteMatrix() {
		
		Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
		Cursor cursor = context.getContentResolver().query(notesUri,
				PROJECTIONALL, null, null, null);
		int totalrow = cursor.getCount();
		long[][] noteArray = new long[totalrow][2];
		cursor.moveToFirst();
		if(totalrow == 0){
			return noteArray;
		}
		if (debug) Log.d(TAG,"test cursor:-> "+cursor.getString(0));
		if (debug) Log.d(TAG,"test cursor long:-> "+cursor.getLong(0));
		for (int i = 0; i < totalrow; i++) {
			noteArray[i][0] = cursor.getLong(0);
			noteArray[i][1] = cursor.getLong(4);
			cursor.moveToNext();
		}
		 return noteArray;
		
		
	}

	private void deleteNotes(String deleteData) {
		try {
			JSONObject jsonMainObj = new JSONObject(deleteData);
			JSONArray jsonArray = jsonMainObj.getJSONArray("data");
			if(jsonArray.length() == 0){
				// this means nothing is to be updated back in OI Notes
				//return;
				if (debug) Log.d(TAG,"Nothing to delete from client:-> "+"");
			}
			
			Ulg.d("[appyres] lenth of del array: "+jsonArray.length());
			for(int i=0;i<jsonArray.length();i++){
				
				JSONObject jobj = jsonArray.getJSONObject(i);
				if (debug) Ulg.d(TAG,"going to delete note from client:-> "+jobj.getLong("id"));
				int localId = jobj.getInt("id");
				
				Uri notes = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
				notes = Uri.withAppendedPath(notes, String.valueOf(localId));
				
				int dels = context.getContentResolver().delete(notes, null, null);
				if (debug) Log.d(TAG,"message:-> "+dels);
			}
		}
		
		catch (Exception e) {
			// TODO: handle exception
		}
				
				
		
	}

	private void insertNote(JSONObject jobj) {
		
		
		try {
			//long localId = jobj.getLong("id"); // just for checking del this
			long gId = jobj.getLong("googleId");
			JSONObject noteobj = jobj.getJSONObject("jsonString");
			
			ContentValues values = new ContentValues();
			values.put(Notes.TITLE, noteobj.getString("title").trim());
			values.put(Notes.NOTE, noteobj.getString("note").trim());
			values.put(Notes.CREATED_DATE, noteobj.getLong("created_date") );
			values.put(Notes.MODIFIED_DATE, noteobj.getLong("modified_date"));
			
			if(!(noteobj.getString("tags").trim().equals("null"))) {
				values.put(Notes.TAGS, noteobj.getString("tags"));
			}
			
			if(!(noteobj.getString("encrypted").trim().equals("null"))) {
				values.put(Notes.ENCRYPTED, noteobj.getLong("encrypted"));
			}
			
			if(!(noteobj.getString("theme").trim().equals("null"))) {
				values.put(Notes.THEME, noteobj.getString("theme"));
			}
			
			values.put(Notes.SELECTION_START, noteobj.getLong("selection_start"));
			values.put(Notes.SELECTION_END, noteobj.getLong("selection_end"));
			values.put(Notes.SCROLL_POSITION, noteobj.getDouble("scroll_position"));
			
			Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
			Uri insertUri = context.getContentResolver().insert(notesUri, values);
			if (debug) Log.v(TAG, "inserted into the notepad: "+insertUri);
			// Now insert the new got Id form insertUri into idMapTable
			String IDMAP_AUTHORITY = "org.openintents.cloudsync.contentprovider";
			
			String IDMAP_BASE_PATH = "idmaps";
			Uri IDMAP_CONTENT_URI = Uri.parse("content://" + IDMAP_AUTHORITY
					+ "/" + IDMAP_BASE_PATH);
			
			String insertRetId = insertUri.getLastPathSegment();
			ContentValues idmapValues = new ContentValues();
			idmapValues.put("localid", Long.parseLong(insertRetId));
			idmapValues.put("appid", gId);
			idmapValues.put("pckname", NotepadSync.PACKAGE_NAME);
			
			context.getContentResolver().insert(IDMAP_CONTENT_URI, idmapValues);
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
	}


	protected void applyPostExecute(String result) {
		
		SharedPreferences prefs = Util.getSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Util.IN_SYNC, false);
		editor.commit();
		
	}
	
	private long[][] getmodMatrix() {
		
		String MOD_AUTHORITY = "org.openintents.cloudsync.contentprovider";
		String MOD_BASE_PATH = "modifys";
		Uri modUri = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
		Cursor modCursor = context.getContentResolver().query(modUri, null, null, null, null);
		
		if(modCursor==null | modCursor.getCount()==0) {
			if (debug) Log.d(TAG, "it is null");
			return new long[0][3];
		}
		modCursor.moveToFirst();
		
		long[][] modMatrix = new long[modCursor.getCount()][3];
		int totRows = modCursor.getCount();
		
		for(int i=0;i<totRows;i++){
			modMatrix[i][0] = Long.parseLong(modCursor.getString(0));
			modMatrix[i][1] = Long.parseLong(modCursor.getString(1));
			modMatrix[i][2] = Long.parseLong(modCursor.getString(2));
			modCursor.moveToNext();
		}
		
		for(int i=0;i<totRows;i++){
			if (debug) Log.d(TAG, "the modMatrix:-> "+modMatrix[i][0]+" "+modMatrix[i][1]+" "+modMatrix[i][2]);
			
		}
		return modMatrix;
	}
	

}
