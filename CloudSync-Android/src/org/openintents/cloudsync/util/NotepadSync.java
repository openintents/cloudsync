package org.openintents.cloudsync.util;

import android.net.Uri;

public class NotepadSync {
	
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

}
