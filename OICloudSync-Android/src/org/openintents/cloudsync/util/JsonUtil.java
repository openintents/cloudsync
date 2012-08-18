package org.openintents.cloudsync.util;


import android.database.Cursor;
import android.util.Log;


public class JsonUtil {

	private static final String TAG = "JsonUtil";
	private static final boolean debug = true;

	public static StringBuilder addToJson(Cursor cursor, StringBuilder jsonBuilder) {
// this methods makes the json string by adding the notes to one json string
		
        String id=cursor.getString(0);
		String title=cursor.getString(1);
		String note=cursor.getString(2);
		String created_date=cursor.getString(3);
		String modified_date=cursor.getString(4);
		String tags=cursor.getString(5);
		String encrypted=cursor.getString(6);
		String theme=cursor.getString(7);
		String selection_start=cursor.getString(8);
		String selection_end=cursor.getString(9);
		String scroll_position=cursor.getString(10);
		
		long LUID = ArrayUtil.getLUID(cursor.getLong(0), cursor.getLong(3));
		String jsonNoteString= " { \"id\": \" "+id+" \" , \"jsonString\": { \"title\": \" "+title+" \", \"note\": \" "+note+" \", \"created_date\": \" "+created_date+" \", \"modified_date\": \" "+modified_date+" \", \"tags\": \" "+tags+" \", \"encrypted\": \" "+encrypted+" \", \"theme\": \" "+theme+" \", \"selection_start\": \" "+selection_start+" \", \"selection_end\": \" "+selection_end+" \",\"scroll_position\": \" "+scroll_position+" \" } }  ";
		jsonBuilder.append(jsonNoteString) ;
		jsonBuilder.append(",");
		
		return jsonBuilder;
	}

	public static StringBuilder addToJsonDel(Long id, StringBuilder jsonDeleteBuilder) {
		
		String jsonNoteString= " { \"id\":"+id+"}";
		if (debug) if (debug) Log.i(TAG,"jsonDelString"+jsonNoteString);
		
		jsonDeleteBuilder.append(jsonNoteString);
		jsonDeleteBuilder.append(",");
		
		return jsonDeleteBuilder;
	}

}
