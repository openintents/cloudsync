//package org.openintents.cloudsync.syncadapter;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//
//public class VishuActivity extends Activity {
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        sync();
//    }
//    
//    void sync() {
//    	Log.d("vincent", "inside the sync");
//    	Log.d("vincent", "the math random is: "+Math.random() );
//    	if(Math.random() > 0.30 ) {
//    		Alarm al = new Alarm();
//    		al.SetAlarm(this);
//    		Log.d("vincent", "the alarm is called");
//    	}
//    }
//}