package org.openintents.cloudsync.syncadapter;

import org.openintents.cloudsync.CloudSyncActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class Alarm extends BroadcastReceiver 
{    
     private static final boolean debug = true;
	private static final String TAG = "Alarm";
	private CloudSyncActivity activity;

	@Override
     public void onReceive(Context context, Intent intent) 
     {   
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
         wl.acquire();
         
         activity.startStandAloneSync();
         if (debug) Log.d(TAG,"Trying the standAloneSync from the broadCast reciever:-> ");
         // Put here YOUR code.         
//         Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
//         double network = Math.random();
//         if(network > .75 ) {
//        	 Log.d("vincent", "the network strenth is: "+network);
//        	 AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        	 Intent i = new Intent(context, Alarm.class);
//        	 am.cancel(PendingIntent.getBroadcast(context, 0, i, 0));
//        	 Log.d("vincent", "tried to cancel the alarm");
//         }

         wl.release();
     }

 public void setAlarm(CloudSyncActivity activity)
 {
	 setActivity(activity);
     AlarmManager am=(AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
     Intent i = new Intent(activity, Alarm.class);
     PendingIntent pi = PendingIntent.getBroadcast(activity, 0, i, 0);
     am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 20, pi); // Millisec * Second * Minute
     if (debug) Log.d(TAG,"The alarm is set in setAlarm() :-> ");
 }

 private void setActivity(CloudSyncActivity activity) {
	 this.activity = activity;
	
}

public void CancelAlarm(Context context)
 {
     Intent intent = new Intent(context, Alarm.class);
     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     alarmManager.cancel(sender);
 }
}
