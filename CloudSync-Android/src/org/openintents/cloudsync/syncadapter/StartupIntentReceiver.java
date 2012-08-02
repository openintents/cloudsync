package org.openintents.cloudsync.syncadapter;

import org.openintents.cloudsync.CloudSyncActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("vincent", "Inside the onRecieve after the boot.");
		
		//CloudSyncActivity activity = new CloudSyncActivity();
		//activity.startStandAloneSync();
		
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("org.openintents.cloudsync.service.StartUpService");
		context.startService(serviceIntent);
		
	}
}
