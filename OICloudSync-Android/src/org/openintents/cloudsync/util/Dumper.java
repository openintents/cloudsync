package org.openintents.cloudsync.util;


import org.openintents.cloudsync.notepad.AsyncDetectChange;
import org.openintents.cloudsync.notepad.NotePad;
import org.openintents.cloudsync.notepad.NotePad.Notes;
import org.openintents.cloudsync.notepad.Ulg;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class Dumper {

	public static final String NOTE_AUTHORITY = "org.openintents.notepad";
	public static final Uri NOTE_CONTENT_URI = Uri.parse("content://"+ NOTE_AUTHORITY + "/notes");
	public static final String MOD_AUTHORITY = "org.openintents.cloudsync.contentprovider";
	public static final String MOD_BASE_PATH = "modifys";	
	public static final Uri MOD_CONTENT_URI = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
	
	public static void updateModTable(Activity activity) {
		
		String[] PROJECTION = new String[] {
		        NotePad.Notes._ID, // 0
		        Notes.CREATED_DATE,
		        Notes.MODIFIED_DATE,
		    };
		
		activity.getContentResolver().delete(MOD_CONTENT_URI, null, null);
		Cursor cursor = activity.getContentResolver().query(NOTE_CONTENT_URI, PROJECTION, null, null, null);
		cursor.moveToFirst();
		
		for (int i = 0; i < cursor.getCount(); i++) {
			long LUID = ArrayUtil.getLUID(cursor.getLong(0),cursor.getLong(1));
			ContentValues values =  new ContentValues();
			values.put("localid", LUID);
			values.put("moddate", cursor.getLong(2));
			values.put("pckname", AsyncDetectChange.PACKAGE_NAME);
			Uri insertUri = activity.getContentResolver().insert(MOD_CONTENT_URI, values);
			Ulg.d("Inseted id: "+LUID+" uri is: "+insertUri.toString());
			cursor.moveToNext();
		}
		
		
		
		
	}

	public static void dump(String msg, long[] array) {
		
		for (long value : array) {
			Ulg.d("Array value [" +msg+ "]: "+value);
		}
		
	}
	
	

}
