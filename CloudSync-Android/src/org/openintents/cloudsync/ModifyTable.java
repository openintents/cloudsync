package org.openintents.cloudsync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ModifyTable {
	
	// Database table
			public static final String TABLE_MODIFY = "modify";
			public static final String COLUMN_ID = "_id";
			public static final String COLUMN_LOCAL_ID = "localid";
			public static final String COLUMN_MODIGY_DATE = "moddate";
			public static final String PACKAGE_NAME = "pckname";
			// Database creation SQL statement
			private static final String DATABASE_CREATE = "create table " 
					+ TABLE_MODIFY
					+ "(" 
					+ COLUMN_ID + " integer primary key autoincrement, " 
					+ COLUMN_LOCAL_ID + " INTEGER, " 
					+ COLUMN_MODIGY_DATE + " INTEGER,"
					+ PACKAGE_NAME 
				    + " text not null" 		
					+ ");";
			
			public static void onCreate(SQLiteDatabase database) {
				database.execSQL(DATABASE_CREATE);
			}

			public static void onUpgrade(SQLiteDatabase database, int oldVersion,
					int newVersion) {
				Log.w(IdMapTable.class.getName(), "Upgrading database from version "
						+ oldVersion + " to " + newVersion
						+ ", which will destroy all old data");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_MODIFY);
				onCreate(database);
			}

}
