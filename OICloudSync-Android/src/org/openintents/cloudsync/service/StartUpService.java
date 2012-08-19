package org.openintents.cloudsync.service;

import org.openintents.cloudsync.Util;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StartUpService extends Service {

	private static final boolean debug = true;
	private static final String TAG = "StartUpService";
	public static final String NOTE_AUTHORITY = "org.openintents.notepad";
	public static final Uri NOTE_CONTENT_URI = Uri.parse("content://"+ NOTE_AUTHORITY + "/notes");

	@Override
	public IBinder onBind(Intent intent) {

		return null;

	}

	@Override
	public void onCreate() {
		super.onCreate();

		Toast.makeText(this, "Service Created Vincent", Toast.LENGTH_LONG).show();
		if (debug) Log.d(TAG,"startup service onCreate:-> ");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed Vincent", Toast.LENGTH_LONG).show();
		

	}

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Toast.makeText(this, "Service Started Vincent", Toast.LENGTH_LONG).show();
		if (debug) Log.d(TAG,"The startup service onStart():-> ");
		
		try {

			Cursor cursor = getContentResolver().query(NOTE_CONTENT_URI, null,
					null, null, null);
			cursor.moveToFirst();
		} catch (NullPointerException e) {

			if (debug)
				Log.d(TAG, "The OI Note is not installed:-> ");
			//helloWorld.setText("The OI Note is not yet Installed");
			return;
		}

		if(Util.isNetworkAvailable(this)) {
						SharedPreferences prefs = Util.getSharedPreferences(this);
						SharedPreferences.Editor editor = prefs.edit();
						boolean inSync = prefs.getBoolean(Util.IN_SYNC, false);
						long nowTime = System.currentTimeMillis();
						long lastTime = prefs.getLong(Util.LAST_TIME, 0);
						if ((nowTime - lastTime) > Util.SYNC_DIFF_TIME | !inSync) {
							editor.putLong(Util.LAST_TIME, nowTime);
							editor.putBoolean(Util.IN_SYNC, true);
							Log.d("vincent", "Do the sync baccha!!");
							editor.commit();
							try {
								DetectChange dc = new DetectChange(this);
								dc.detectExecute();
							} catch (Exception e) {
								if (debug) Log.e(TAG,"An Exception has occured serious:-> ");
								Log.e(TAG, "Exceoption in Service for sync" , e);
										
							}
							
						} else {
							if (debug) Log.d(TAG,"The sync happened within two minutes: ");
						}
						
					} else {
						if (debug) Log.d(TAG,"The network is not presently available:-> ");
					}
		
		
//		AsyncDetectChange adc = new AsyncDetectChange(this);
//		adc.execute();
		if (debug) Log.d(TAG,"Going to destroy the service:-> ");
		onDestroy();

	}
}