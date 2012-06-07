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

public class ModifyContentProvider extends ContentProvider {
	
	// database
		private SyncDatabaseHelper database;

		// Used for the UriMacher
		private static final int MODIFYS = 10;
		private static final int MODIFY_ID = 20;

		private static final String AUTHORITY = "org.openintents.mods.contentprovider";

		private static final String BASE_PATH = "modifys";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + BASE_PATH);
		
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/modifys";
		
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/modify";
		
		private static final UriMatcher sURIMatcher = new UriMatcher(
				UriMatcher.NO_MATCH);
		static {
			sURIMatcher.addURI(AUTHORITY, BASE_PATH, MODIFYS);
			sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MODIFY_ID);
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
			queryBuilder.setTables(ModifyTable.TABLE_MODIFY);

			int uriType = sURIMatcher.match(uri);
			
			switch (uriType) {
			case MODIFYS:
				break;
			case MODIFY_ID:
				// Adding the ID to the original query
				queryBuilder.appendWhere(ModifyTable.COLUMN_ID + "="
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

		private void checkColumns(String[] projection) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Uri insert(Uri uri, ContentValues values) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsDeleted = 0;
			long id = 0;
			switch (uriType) {
			case MODIFYS:
				id = sqlDB.insert(ModifyTable.TABLE_MODIFY, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(BASE_PATH + "/" + id);
		}

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsDeleted = 0;
			switch (uriType) {
			case MODIFYS:
				rowsDeleted = sqlDB.delete(ModifyTable.TABLE_MODIFY, selection,
						selectionArgs);
				break;
			case MODIFY_ID:
				String id = uri.getLastPathSegment();
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
			case MODIFYS:
				rowsUpdated = sqlDB.update(ModifyTable.TABLE_MODIFY, 
						values, 
						selection,
						selectionArgs);
				break;
			case MODIFY_ID:
				String id = uri.getLastPathSegment();
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
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
		
				
				getContext().getContentResolver().notifyChange(uri, null);
				return rowsUpdated;
	}
			
		

}

		
