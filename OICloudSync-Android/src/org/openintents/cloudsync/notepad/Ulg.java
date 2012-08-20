package org.openintents.cloudsync.notepad;

import android.util.Log;

public class Ulg {
	static boolean extDebug= true; 
	public static void d(String TAG, String message) {
		if(extDebug) Log.d(TAG, message);
	}
	
	public static void v(String TAG, String message) {
		if(extDebug)Log.v(TAG, message);
	}
	
	public static void i(String TAG, String message) {
		if(extDebug)Log.i(TAG, message);
	}
	
	public static void i(String message) {
		if(extDebug)Log.i("debugging", message);
	}
	
	public static void d(String message) {
		if(extDebug)Log.d("debugging", message);
	}
	

}
