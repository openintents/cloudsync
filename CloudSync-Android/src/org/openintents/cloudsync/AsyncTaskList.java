package org.openintents.cloudsync;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.google.web.bindery.requestfactory.shared.Violation;

import org.openintents.cloudsync.shared.CloudSyncRequest;
import org.openintents.cloudsync.shared.CloudSyncRequestFactory;
import org.openintents.cloudsync.shared.TaskProxy;


public class AsyncTaskList extends AsyncTask<String[][], Void, String[][] > {
	private static final String TAG = "CloudSyncActivity";
	CloudSyncActivity activity;
	static String tag = "vincent";
	
	public AsyncTaskList(CloudSyncActivity activity) {
		this.activity = activity;
	}

	@Override
	protected String[][] doInBackground(String[][]... params) {
		
		Log.i("vincent", "inside the async");
		String[][] clientArray = params[0];
		
		final List<TaskProxy> list = new ArrayList<TaskProxy>();		
		CloudSyncRequestFactory factory = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
		factory.taskRequest().queryTasks().fire(new Receiver<List<TaskProxy>>() {

			@Override
			public void onSuccess(List<TaskProxy> arg0) {
				Log.i("vincent", "inside succes of Async");
				
				
				Log.d(tag, "Size of list"+list.size());
				list.addAll(arg0);
				Log.d(tag, "Size of list"+list.size());
				
			} 
			
			
		});
		
		Log.i("vincent", "async completed taking the list");
		int il = list.size();
		Log.d(TAG, "inside Asynctasklist: size of list returned from server"+list.size());
		
		String[] [] serverArr = null;
		
		if(il==0) { 
			Log.d(TAG, "size of taskList returned from server is 0");
			} 
		
		else {
			for(TaskProxy task:list) {
				Log.d(TAG, ""+task.getId());
				Log.d(TAG, ""+task.getLocallyGeneratedUID());				
				Log.d(TAG, ""+task.getTimestamp());
				Log.d(TAG, ""+task.getTag());
				Log.d(TAG, ""+task.getAppPackageName());
				Log.d(TAG, ""+task.getJsonStringData());
				
			}		
			
		}	
		
		
        CloudSyncRequest request = factory.taskRequest();      
        long lng = new Long(1254555252);
        Log.d(TAG,"going to write random stuff");
       
        TaskProxy[] task = new TaskProxy[2]; 
        Log.i(TAG,"made the taskproxy array");	
        
        task[0] = request.create(TaskProxy.class);
    	task[0].setLocallyGeneratedUID(lng+562);    	
    	Calendar cal = Calendar.getInstance();    	
    	task[0].setTimestamp(cal.getTimeInMillis());
    	task[0].setTag('c');
    	task[0].setAppPackageName("com.google.com");
    	task[0].setJsonStringData("this must be very very long string in randome order");
    	
    	Log.d(TAG, "made the first task proxy");
    	task[1] = request.create(TaskProxy.class);
    	
    	
    	task[1].setLocallyGeneratedUID(lng);    	
    	cal = Calendar.getInstance();    	
    	task[1].setTimestamp(cal.getTimeInMillis());
    	task[1].setTag('c');
    	task[1].setAppPackageName("com.google.com");
    	task[1].setJsonStringData("this must be very very long string in randome order");
    	
    	request.updateTask(task[0]);
    	request.updateTask(task[1]);
    	request.fire();
    	Log.d(TAG, "Fire is done");
    	
    	CloudSyncRequestFactory factory1 = Util.getRequestFactory(activity, CloudSyncRequestFactory.class);
    	factory1.taskRequest().getAppEngineTime().fire(new Receiver<Long>() {

			@Override
			public void onSuccess(Long time) {
				Log.i("vincent", "inside succes of Async for getting the time from server");
				Log.i("vincent", "long time is"+time.toString());
				// new calendar inside the appEngine doesnot work
				// use new Date() or System.getmilliseconds
				
				
			}

			@Override
			public void onFailure(ServerFailure error) {
				Log.i("vincent", "it has failed");
				super.onFailure(error);
			}

			@Override
			public void onViolation(Set<Violation> errors) {
				Log.i("vincent", "did some violation");
				super.onViolation(errors);
			}
			
			
			
			
		});
    	return null;
	}

	@Override
	protected void onPostExecute(String[][] result) {
		Log.d(tag, "inside the post execute");	
		activity.doneSyncing();
		super.onPostExecute(result);
		for(int i=0;i<10;i++) {
			Calendar cal = Calendar.getInstance();
			Log.i(TAG, ""+cal.getTimeInMillis());
		}
	}
	
	/**
	@Override
	protected void onPostExecute(String[][] serverArr) {
		Log.d(TAG, "onPostExecute() of asynctasklist" );
		activity.setServerArr(serverArr);
	}
	*/

	
}
