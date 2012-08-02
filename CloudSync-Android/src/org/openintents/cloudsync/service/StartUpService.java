package org.openintents.cloudsync.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StartUpService extends Service {

	private static final boolean debug = true;
	private static final String TAG = "StartUpService";

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
		
		AsyncDetectChange adc = new AsyncDetectChange(this);
		adc.execute();

	}
}