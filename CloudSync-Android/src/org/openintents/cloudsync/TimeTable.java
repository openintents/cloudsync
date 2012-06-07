package org.openintents.cloudsync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TimeTable {
	public static final String TABLE_TIME = "time";
	public static final String COLUMN_ID = "_id";
	public static final String TIMESTAMP = "timestamp";
	public static final String PACKAGE_NAME = "pckname";
	
	// Database creation SQL statement
		private static final String DATABASE_CREATE = "create table " 
				+ TABLE_TIME
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, " 
				+ TIMESTAMP + " INTEGER,"
				+ PACKAGE_NAME 
				+ " text not null" 
				+ ");";
		
		public static void onCreate(SQLiteDatabase database) {
			database.execSQL(DATABASE_CREATE);
		}
		
		public static void onUpgrade(SQLiteDatabase database, int oldVersion,
				int newVersion) {
			Log.w(TimeTable.class.getName(), "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME);
			onCreate(database);
		}

}
