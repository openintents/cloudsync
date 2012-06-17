package org.openintents.cloudsync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class CloudSyncContentProvider extends ContentProvider{
	
	// database
	private SyncDatabaseHelper database;
	
	// Used for the UriMacher
	private static final int IDMAPS = 10;
	private static final int IDMAP_ID = 20;
	
	private static final int MODIFYS = 10;
	private static final int MODIFY_ID = 20;
	
	private static final int TIMES = 10;
	private static final int TIME_ID = 20;
	
	private static final String AUTHORITY = "org.openintents.cloudsync.contentprovider";
	
	private static final String IDMAPS_BASE_PATH = "idmaps";
	public static final Uri IDMAPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + IDMAPS_BASE_PATH);
		
	private static final String MODIFY_BASE_PATH = "modifys";
	public static final Uri MODIFY_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + MODIFY_BASE_PATH);
	
	private static final String TIME_BASE_PATH = "times";
	public static final Uri TIME_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TIME_BASE_PATH);
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sURIMatcher.addURI(AUTHORITY, MODIFY_BASE_PATH, MODIFYS);
		sURIMatcher.addURI(AUTHORITY, MODIFY_BASE_PATH + "/#", MODIFY_ID);
		
		sURIMatcher.addURI(AUTHORITY, IDMAPS_BASE_PATH, IDMAPS);
		sURIMatcher.addURI(AUTHORITY, IDMAPS_BASE_PATH + "/#", IDMAP_ID);
		
		sURIMatcher.addURI(AUTHORITY, TIME_BASE_PATH, TIMES);
		sURIMatcher.addURI(AUTHORITY, TIME_BASE_PATH + "/#", TIME_ID);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

	

}
