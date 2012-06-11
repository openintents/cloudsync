package org.openintents.cloudsync;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class IdMapContentProvider extends ContentProvider{
	// database
		private SyncDatabaseHelper database;

		// Used for the UriMacher
		private static final int IDMAPS = 10;
		private static final int IDMAP_ID = 20;

		private static final String AUTHORITY = "org.openintents.idmap.contentprovider";
		
		private static final String BASE_PATH = "idmaps";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + BASE_PATH);
		
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/idmaps";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/idmap";

		private static final UriMatcher sURIMatcher = new UriMatcher(
				UriMatcher.NO_MATCH);
		
		static {
			sURIMatcher.addURI(AUTHORITY, BASE_PATH, IDMAPS);
			sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", IDMAP_ID);
		}
		
		@Override
		public boolean onCreate() {
			database = new SyncDatabaseHelper(getContext());			
			return false;
		}
		
		@Override
		public Cursor query(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {

			// Using SQLiteQueryBuilder instead of query() method
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

			// Check if the caller has requested a column which does not exists
			checkColumns(projection);

			// Set the table
			queryBuilder.setTables(IdMapTable.TABLE_IDMAP);
			
			int uriType = sURIMatcher.match(uri);
			switch (uriType) {
			case IDMAPS:
				break;
			case IDMAP_ID:
				// Adding the ID to the original query
				queryBuilder.appendWhere(IdMapTable.COLUMN_ID + "="
						+ uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}

			SQLiteDatabase db = database.getWritableDatabase();
			Cursor cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			// Make sure that potential listeners are getting notified
			cursor.setNotificationUri(getContext().getContentResolver(), uri);

			return cursor;
		}
		
		@Override
		public String getType(Uri uri) {
			return null;
		}
		
		@Override
		public Uri insert(Uri uri, ContentValues values) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsDeleted = 0;
			long id = 0;
			switch (uriType) {
			case IDMAPS:
				id = sqlDB.insert(IdMapTable.TABLE_IDMAP, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(BASE_PATH + "/" + id);
		}

		private void checkColumns(String[] projection) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsDeleted = 0;
			switch (uriType) {
			case IDMAPS:
				rowsDeleted = sqlDB.delete(IdMapTable.TABLE_IDMAP, selection,
						selectionArgs);
				break;
			case IDMAP_ID:
				String id = uri.getLastPathSegment();
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
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsDeleted;
		}

		@Override
		public int update(Uri uri, ContentValues values, String selection,
				String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsUpdated = 0;
			switch (uriType) {
			case IDMAPS:
				rowsUpdated = sqlDB.update(IdMapTable.TABLE_IDMAP, 
						values, 
						selection,
						selectionArgs);
				break;
			
			case IDMAP_ID:
				String id = uri.getLastPathSegment();
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
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsUpdated;
			
		}

}
