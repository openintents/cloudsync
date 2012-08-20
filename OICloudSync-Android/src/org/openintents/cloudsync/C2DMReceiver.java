/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openintents.cloudsync;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import java.io.IOException;
import java.util.TimerTask;

import org.openintents.cloudsync.client.MyRequestFactory;
import org.openintents.cloudsync.client.MyRequestFactory.HelloWorldRequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Receive a push message from the Cloud to Device Messaging (C2DM) service.
 * This class should be modified to include functionality specific to your
 * application. This class must have a no-arg constructor and pass the sender id
 * to the superclass constructor.
 */
public class C2DMReceiver extends C2DMBaseReceiver {
	
    private static final boolean debug = true;
	private static final String TAG = "C2DMReceiver";
	CDMTimer cdmTimeTask = new CDMTimer();
    Handler timeHandle = new Handler();
    C2DMReceiver receiver;

    public void setReceiver(C2DMReceiver receiver) {
		this.receiver = receiver;
	}

	public C2DMReceiver() {
        super(Setup.SENDER_ID);
    }

    /**
     * Called when a registration token has been received.
     * 
     * @param context the Context
     * @param registrationId the registration id as a String
     * @throws IOException if registration cannot be performed
     */
    @Override
    public void onRegistered(Context context, String registration) {
        DeviceRegistrar.registerOrUnregister(context, registration, true);
    }

    /**
     * Called when the device has been unregistered.
     * 
     * @param context the Context
     */
    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Util.getSharedPreferences(context);
        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        DeviceRegistrar.registerOrUnregister(context, deviceRegistrationID, false);
    }

    /**
     * Called on registration error. This is called in the context of a Service
     * - no dialog or UI.
     * 
     * @param context the Context
     * @param errorId an error message, defined in {@link C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
        context.sendBroadcast(new Intent(Util.UPDATE_UI_INTENT));
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        /*
         * Replace this with your application-specific code
         */
    	SharedPreferences prefs = Util.getSharedPreferences(this);
    	String storedMessageId = prefs.getString(Util.C2DMID, "nothing");
    	String storedDeviceId = prefs.getString(Util.DEVICE_REGISTRATION_ID, "no device id");
    	Bundle extras = intent.getExtras();
    	String gotMessage = "";
        if (extras != null) {
            gotMessage = (String) extras.get("message");
        }
        
        if (debug) Log.d(TAG,"storedMsg: "+storedMessageId+" GotMessage:-> "+gotMessage);
        if(storedDeviceId.equals(gotMessage)){
        	MessageDisplay.displayMessage(context, intent);
        	if (debug) Log.d(TAG,"Same sender and reciever:-> "+gotMessage);
        } else {
        	MessageDisplay.displayMessage(context, intent);
        	if (debug) Log.d(TAG,"C2DM message came from diff device:-> ");
        	
        	// Going to start the sync service in the async task.
            setReceiver(this);
        	new AsyncTask<Void, Void, String>() {
                private String message;

                @Override
                protected String doInBackground(Void... arg0) {
                	Intent serviceIntent = new Intent();
    				serviceIntent.setAction("org.openintents.cloudsync.service.StartUpService");
    				receiver.startService(serviceIntent);
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    
                }
            }.execute();
        }
        
        
        
//        cdmTimeTask.setContext(this);
//        cdmTimeTask.setHandle(timeHandle);
//        timeHandle.removeCallbacks(cdmTimeTask);
//        timeHandle.postDelayed(cdmTimeTask, 30000);
        
    }
}

class CDMTimer extends TimerTask {
	private static final boolean debug = true;
	private static final String TAG = "UpdateTimeTask";
	private Context context;
	private Handler handle;

	public void run() {
		
		   if (debug) Log.d(TAG,"The sync is going to run from C2DM reciever:-> ");
		   SharedPreferences prefs = Util.getSharedPreferences(context);
		   boolean inSync = prefs.getBoolean(Util.IN_SYNC, false);
		   long nowTime = System.currentTimeMillis();
		   long lastTime = prefs.getLong(Util.LAST_TIME, 0);
		   if( (nowTime-lastTime) > Util.SYNC_DIFF_TIME | !inSync) {
			  
			   if (debug) Log.d(TAG,"Conditions are right for the sync!!:-> ");
			    Intent serviceIntent = new Intent();
				serviceIntent.setAction("org.openintents.cloudsync.service.StartUpService");
				context.startService(serviceIntent);
				
		   } else {
			   // I hope when a sync is rejected because already a sync is taking place then
			   // using the handle that was passed passing this object itself and delay with 120 secs.
			   if (debug) Log.d(TAG,"the sync is called by C2dm reciever but time diff is not enough its:-> "+(nowTime-lastTime)/1000);
			   handle.removeCallbacks(this);
			   handle.postDelayed(this, 120000);
		   }
		   
		   Log.d("vincent", "Time diff was: "+(nowTime-lastTime)/1000);
		   
	       
	   }

	public void setHandle(Handler timeHandle) {
		
		this.handle = timeHandle;
		
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	}
    