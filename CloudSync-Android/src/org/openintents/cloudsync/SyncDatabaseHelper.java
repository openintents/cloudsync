package org.openintents.cloudsync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SyncDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "syncsecond.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "SyncDatabaseHelper";
	private static final boolean debug = true;

	public SyncDatabaseHelper(Context context) {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if(debug) Log.d(TAG, "inside syncdatabase helper on create");
		TimeTable.onCreate(db);
		IdMapTable.onCreate(db);
		ModifyTable.onCreate(db);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		TimeTable.onUpgrade(db, oldVersion, newVersion);
		IdMapTable.onUpgrade(db, oldVersion, newVersion);
		ModifyTable.onUpgrade(db, oldVersion, newVersion);
	}

}
