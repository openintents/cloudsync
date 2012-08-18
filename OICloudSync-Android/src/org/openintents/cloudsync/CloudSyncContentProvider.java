package org.openintents.cloudsync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CloudSyncContentProvider extends ContentProvider{
	
	// database
	private SyncDatabaseHelper database;
	
	// Used for the UriMacher
	private static final int IDMAPS = 10;
	private static final int IDMAP_ID = 20;
	
	private static final int MODIFYS = 30;
	private static final int MODIFY_ID = 40;
	
	private static final int TIMES = 50;
	private static final int TIME_ID = 60;
	
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
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		String id = "";
		switch (uriType) {
		case MODIFYS:
			rowsDeleted = sqlDB.delete(ModifyTable.TABLE_MODIFY, selection,
					selectionArgs);
			break;
			
		case MODIFY_ID	:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(ModifyTable.TABLE_MODIFY,
						ModifyTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(ModifyTable.TABLE_MODIFY,
						ModifyTable.COLUMN_ID + "=" + id 
						+ " and " + selection,
						selectionArgs);
			}
			break;
		
		case IDMAPS:
			rowsDeleted = sqlDB.delete(IdMapTable.TABLE_IDMAP, selection,
					selectionArgs);
			break;
			
		case IDMAP_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(IdMapTable.TABLE_IDMAP,
						IdMapTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(IdMapTable.TABLE_IDMAP,
						IdMapTable.COLUMN_ID + "=" + id 
						+ " and " + selection,
						selectionArgs);
			}
			break;
			
		case TIMES:
			rowsDeleted = sqlDB.delete(TimeTable.TABLE_TIME, selection,
					selectionArgs);
			break;
			
		case TIME_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(TimeTable.TABLE_TIME,
						TimeTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(TimeTable.TABLE_TIME,
						TimeTable.COLUMN_ID + "=" + id 
						+ " and " + selection,
						selectionArgs);
			}
			break;
			
		
			
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		String retBasePath ="";
		switch (uriType) {
		case MODIFYS:
			id = sqlDB.insert(ModifyTable.TABLE_MODIFY, null, values);
			retBasePath = MODIFY_BASE_PATH;
			break;
			
		case TIMES:
			id = sqlDB.insert(TimeTable.TABLE_TIME, null, values);
			retBasePath = TIME_BASE_PATH;
			break;
		
		case IDMAPS:
			id = sqlDB.insert(IdMapTable.TABLE_IDMAP, null, values);
			retBasePath = IDMAPS_BASE_PATH;
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse( retBasePath + "/" + id);
	}

	@Override
	public boolean onCreate() {
		database = new SyncDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);
		
		SQLiteDatabase db;
		Cursor cursor;
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case MODIFYS:
			queryBuilder.setTables(ModifyTable.TABLE_MODIFY);
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		
		case MODIFY_ID:
			// Adding the ID to the original query
			queryBuilder.setTables(ModifyTable.TABLE_MODIFY);
			
			queryBuilder.appendWhere(ModifyTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		
		case IDMAPS:
			queryBuilder.setTables(IdMapTable.TABLE_IDMAP);
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case IDMAP_ID:
			queryBuilder.setTables(IdMapTable.TABLE_IDMAP);
			
			// Adding the ID to the original query
			queryBuilder.appendWhere(IdMapTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
			
		case TIMES:
			queryBuilder.setTables(TimeTable.TABLE_TIME);
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case TIME_ID:
			queryBuilder.setTables(TimeTable.TABLE_TIME);
			
			// Adding the ID to the original query
			queryBuilder.appendWhere(TimeTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			
			db = database.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);	
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;	
		
	}

	private void checkColumns(String[] projection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		String id = "";
		
		switch (uriType) {
		case IDMAPS:
			rowsUpdated = sqlDB.update(IdMapTable.TABLE_IDMAP, 
					values, 
					selection,
					selectionArgs);
			break;
		case IDMAP_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(IdMapTable.TABLE_IDMAP, 
						values,
						IdMapTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(IdMapTable.TABLE_IDMAP, 
						values,
						IdMapTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			break;
			
		case MODIFYS:
			rowsUpdated = sqlDB.update(ModifyTable.TABLE_MODIFY, 
					values, 
					selection,
					selectionArgs);
			break;
		case MODIFY_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ModifyTable.TABLE_MODIFY, 
						values,
						ModifyTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(ModifyTable.TABLE_MODIFY, 
						values,
						ModifyTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			break;
			
		case TIMES:
			rowsUpdated = sqlDB.update(TimeTable.TABLE_TIME, 
					values, 
					selection,
					selectionArgs);
			break;
		case TIME_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(TimeTable.TABLE_TIME, 
						values,
						TimeTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(TimeTable.TABLE_TIME, 
						values,
						TimeTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			break;
			
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
		
	}
	
	

	

}
