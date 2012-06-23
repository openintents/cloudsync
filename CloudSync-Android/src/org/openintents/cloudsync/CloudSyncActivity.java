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

import org.openintents.cloudsync.client.MyRequestFactory;
import org.openintents.cloudsync.client.MyRequestFactory.HelloWorldRequest;
import org.openintents.cloudsync.shared.CloudSyncRequestFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;


/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class CloudSyncActivity extends Activity {
    /**
     * Tag for logging.
     */
    private static final String TAG = "CloudSyncActivity";
    private static final boolean debug = true;
    private static Integer val;

    /**
     * The current context.
     */
    private Context mContext = this;

    /**
     * A {@link BroadcastReceiver} to receive the response from a register or
     * unregister request, and to update the UI.
     */
    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String accountName = intent.getStringExtra(DeviceRegistrar.ACCOUNT_NAME_EXTRA);
            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA,
                    DeviceRegistrar.ERROR_STATUS);
            String message = null;
            String connectionStatus = Util.DISCONNECTED;
            if (status == DeviceRegistrar.REGISTERED_STATUS) {
                message = getResources().getString(R.string.registration_succeeded);
                connectionStatus = Util.CONNECTED;
            } else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
                message = getResources().getString(R.string.unregistration_succeeded);
            } else {
                message = getResources().getString(R.string.registration_error);
            }

            // Set connection status
            SharedPreferences prefs = Util.getSharedPreferences(mContext);
            prefs.edit().putString(Util.CONNECTION_STATUS, connectionStatus).commit();

            // Display a notification
            Util.generateNotification(mContext, String.format(message, accountName));
        }
    };

    /**
     * Begins the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (debug) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Register a receiver to provide register/unregister notifications
        registerReceiver(mUpdateUIReceiver, new IntentFilter(Util.UPDATE_UI_INTENT));
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String connectionStatus = prefs.getString(Util.CONNECTION_STATUS, Util.DISCONNECTED);
        if (Util.DISCONNECTED.equals(connectionStatus)) {
            startActivity(new Intent(this, AccountsActivity.class));
        }
        setScreenContent(R.layout.hello_world);
    }

    /**
     * Shuts down the activity.
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateUIReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // Invoke the Register activity
        menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
        return true;
    }

    // Manage UI Screens

    private void setHelloWorldScreenContent() {
        setContentView(R.layout.hello_world);

        final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
        final Button sayHelloButton = (Button) findViewById(R.id.say_hello);
        final Button deleteAll = (Button) findViewById(R.id.delete_all);
        
        
        
        /**
        Uri customUri = Uri.parse(IdMapContentProvider.CONTENT_URI.toString());
        if (debug) Log.d(TAG, "customeUri:-> "+customUri.toString());
        
        ContentValues values = new ContentValues();
        values.put(IdMapTable.COLUMN_LOCAL_ID,100000+Math.round((Math.random()*30)));
        values.put(IdMapTable.COLUMN_APPENG_ID,Math.round((Math.random()*30)) );
        values.put(IdMapTable.PACKAGE_NAME,"com.vettukal.oi");
        
        Uri insertUri;
        insertUri = getContentResolver().insert(customUri, values);
        if (debug) Log.d(TAG, "inserted uri custom return:-> "+insertUri.toString());
        
        Cursor customcurse = getContentResolver().query(customUri, null, null, null, null);
        if (customcurse.moveToFirst()) { }

        int totRows = customcurse.getCount();
        if (debug) Log.d(TAG, "totalrow:-> "+totRows );
        for(int i=0;i<totRows;i++) {
        	if (debug) Log.d(TAG, customcurse.getString(0));
        	if (debug) Log.d(TAG, customcurse.getString(1));
        	if (debug) Log.d(TAG, customcurse.getString(2));
            customcurse.moveToNext();
        }
        
        /**
        Uri deleteUri = Uri.parse(ModifyContentProvider.CONTENT_URI.toString());
        deleteUri = Uri.withAppendedPath(deleteUri, "1");
        int delRetVal = getContentResolver().delete(deleteUri, null, null);
        if (debug) Log.d(TAG, "the return value from the deleted op is : "+delRetVal);
        */
        
        /**
        Uri modUri = Uri.parse(ModifyContentProvider.CONTENT_URI.toString());
        values = new ContentValues();
        values.put(ModifyTable.COLUMN_LOCAL_ID, 0156);
        values.put(ModifyTable.COLUMN_MODIGY_DATE, 0154345);
        insertUri = getContentResolver().insert(modUri, values);
        if (debug) Log.d(TAG, "inserted Mod uri is "+insertUri.toString());
        
         modUri = Uri.parse(ModifyContentProvider.CONTENT_URI.toString());
        modUri = Uri.withAppendedPath(modUri, "1");
        values = new ContentValues();
        values.put(ModifyTable.COLUMN_LOCAL_ID, 56);
        values.put(ModifyTable.COLUMN_MODIGY_DATE, 54345);
        int retVal = getContentResolver().update(modUri, values, null, null);
        if (debug) Log.d(TAG, "after update on 1 _id the return value is:-> "+retVal);
        
        modUri = Uri.parse(ModifyContentProvider.CONTENT_URI.toString());
        
        customcurse = getContentResolver().query(modUri, null, null, null, null);
        if (customcurse.moveToFirst()) { }

        totRows = customcurse.getCount();
        if (debug) Log.d(TAG, "totalrow in modtable:-> "+totRows );
        for(int i=0;i<totRows;i++) {
        	if (debug) Log.d(TAG, customcurse.getString(0));
        	if (debug) Log.d(TAG, customcurse.getString(1));
        	if (debug) Log.d(TAG, customcurse.getString(2));
            customcurse.moveToNext();
        }
        */
        
        // Lets print all the contents of the tables.
        Cursor timeCur = getContentResolver().query(CloudSyncContentProvider.TIME_CONTENT_URI, null, null, null, null);
        Cursor idmapCur = getContentResolver().query(CloudSyncContentProvider.IDMAPS_CONTENT_URI, null, null, null, null);
        Cursor cloudMod = getContentResolver().query(CloudSyncContentProvider.MODIFY_CONTENT_URI, null, null, null, null);
        
        timeCur.moveToFirst(); idmapCur.moveToFirst(); cloudMod.moveToFirst();
        if (debug) Log.d(TAG, "time table");
        for(int i =0;i<timeCur.getCount();i++) {
        	if (debug) Log.d(TAG, "_id: "+timeCur.getString(0)+" : "+timeCur.getString(1));
        	timeCur.moveToNext();
        }
        
        if (debug) Log.d(TAG, "idmap table");
        for(int i =0;i<idmapCur.getCount();i++) {
        	if (debug) Log.d(TAG, "_id: "+idmapCur.getString(0)+" : "+idmapCur.getString(1)+" : "+idmapCur.getString(2));
        	idmapCur.moveToNext();
        }
        
        if (debug) Log.d(TAG, "The cloud mod table");
        for(int i =0;i<cloudMod.getCount();i++) {
        	if (debug) Log.d(TAG, "_id: "+cloudMod.getString(0)+" : "+cloudMod.getString(1)+" : "+cloudMod.getString(2));
        	cloudMod.moveToNext();
        }
        
        
        //deleteAll.setVisibility(Button.INVISIBLE);
        deleteAll.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				Uri timeDel = Uri.parse(CloudSyncContentProvider.TIME_CONTENT_URI.toString());
				Uri modDel = Uri.parse(CloudSyncContentProvider.MODIFY_CONTENT_URI.toString());
				Uri idDel = Uri.parse(CloudSyncContentProvider.IDMAPS_CONTENT_URI.toString());
				
				int timeDelret = getContentResolver().delete(timeDel, null, null);
				int modret = getContentResolver().delete(modDel, null, null);
				int idret = getContentResolver().delete(idDel, null, null);
				
				if (debug) Log.d(TAG, "returned vals "+timeDelret+" "+modret+" "+idret);
				
			}
		});
        
        sayHelloButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sayHelloButton.setEnabled(false);
                helloWorld.setText(R.string.contacting_server);

                // Use an AsyncTask to avoid blocking the UI thread
                new AsyncTask<Void, Void, String>() {
                    private String message;

                    @Override
                    protected String doInBackground(Void... arg0) {
                        MyRequestFactory requestFactory = Util.getRequestFactory(mContext,
                                MyRequestFactory.class);
                        final HelloWorldRequest request = requestFactory.helloWorldRequest();
                        if (debug) Log.d(TAG, "Sending request to server");
                        request.getMessage().fire(new Receiver<String>() {
                            @Override
                            public void onFailure(ServerFailure error) {
                                message = "Failure: " + error.getMessage();
                            }

                            @Override
                            public void onSuccess(String result) {
                                message = result;
                            }
                        });
                        return message;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        helloWorld.setText(result);
                        sayHelloButton.setEnabled(true);
                    }
                }.execute();
            }
        });
        
        final Button syncButton = (Button) findViewById(R.id.sync_test);
        syncButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	helloWorld.setText("Syncing Process Started");
            	startSync();
            }
			       
        });
    }
    
    public void purgeOnServer(View v) {
    	if (debug) Log.i(TAG,"going to delete data on Server");
    	if (debug) Log.d(TAG,"The caling package is:-> "+getCallingPackage());
    	if(getCallingPackage()==null) {
    		//TODO: it has to be checked whether the calling package is valid or not.
    		final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
    		helloWorld.setText("Call me from OI Note to delete to Continue");
    		return;
    	}
    	
    	
    	LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.pop_up, null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		popupView.setBackgroundColor(Color.BLACK);
		popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
		
		Button btnDismiss = (Button) popupView.findViewById(R.id.cancel);
		final Button btnYes = (Button) popupView.findViewById(R.id.confirm);
		final CheckBox checkb = (CheckBox) popupView.findViewById(R.id.ck_del);
		checkb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(checkb.isChecked()==true) {
					btnYes.setEnabled(true);	
					}
					else {
						btnYes.setEnabled(false);
					}
				
			}
		});
		
		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				popupWindow.dismiss();
			}
		});
		
		btnYes.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
				purgeAllRecords();
				
			}
		});
    }
		
	/**
     * Sets the screen content based on the screen id.
     */
    private void setScreenContent(int screenId) {
        setContentView(screenId);
        switch (screenId) {
            case R.layout.hello_world:
                setHelloWorldScreenContent();
                break;
        }
    }
    
    private void startSync() {
    	
    	Bundle extras = getIntent().getExtras();
    	if (debug) Log.d(TAG,"intent notesyndemo"+getIntent().describeContents()+"and action is:"+getIntent().getAction());
    	
    	String jsonData = "";
    	if(getIntent().getAction().equalsIgnoreCase("vincent.start")) {
    		// Get the client data 
    		jsonData = extras.getString("data");
    		AsyncTaskList atl = new AsyncTaskList(this);  
        	
            atl.execute(jsonData);
    	}
    	else {
    		
    	}
    	
		
	}
    
    void doneSyncing() {
    	final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
    	final Button syncButton = (Button) findViewById(R.id.sync_test);
    	helloWorld.setText("Finished!");
    	syncButton.setClickable(true);
	}

	public void sendResult(String result) {
		
		Intent i = new Intent();
		i.putExtra("jsonData", result);
		setResult(0,i);
		finish();
	}
	
	protected void purgeAllRecords() {
		
		final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
		helloWorld.setText("Deleting all the records from server");
		// Use an AsyncTask to avoid blocking the UI thread
		final CloudSyncRequestFactory deleteFactory = Util.getRequestFactory(this, CloudSyncRequestFactory.class);
        new AsyncTask<Void, Void, String>() {
            private String message;

            @Override
            protected String doInBackground(Void... arg0) {
            	deleteFactory.taskRequest().deleteAll(getCallingPackage()).fire(new Receiver<Integer>() {

        			@Override
        			public void onSuccess(Integer deleteRowCount) {
        				val=deleteRowCount;
        			
        			}
            	});
            	message = val.toString();
                return message;
            }

            @Override
            protected void onPostExecute(String result) {
            	helloWorld.setText("Done! Number of entries deleted is: "+val);
            }
        }.execute();
    	
    	
    	
	}

	
}
