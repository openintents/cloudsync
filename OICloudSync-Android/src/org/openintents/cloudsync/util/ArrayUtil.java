package org.openintents.cloudsync.util;

import org.openintents.cloudsync.notepad.AsyncDetectChange;

public class ArrayUtil {

	private static final long MILLI_SEC_FACTOR = (long) Math.pow(10, 14);

	public static long[] getSingleDimenArray(long[][] array,
			int returnColumnIndex) {
		
		long[] targetArray = new long[array.length]; 
		for (int i = 0; i < array.length; i++) {
			targetArray[i] = array[i][returnColumnIndex];
		}
		return targetArray;
	}

	public static long[] getLocalIdArrayFromLUIDArray(long[] LUIDArray) {
		
		//1.324512×10¹²
		long[] localId = new long[LUIDArray.length];
		for (int i = 0; i < LUIDArray.length; i++) {
			localId[i] = getLocalIdFromLUID(LUIDArray[i]);
		}
		return localId;
	}

	public static long[] makeLUIDArray(long[][] noteArray) {
		
		long[] noteLUIDArray = new long[noteArray.length];
		
		for (int i = 0; i < noteArray.length; i++) {
			noteLUIDArray[i] = noteArray[i][AsyncDetectChange.NOTE_ARRAY_LOCAL_ID]
					* MILLI_SEC_FACTOR
					+ noteArray[i][AsyncDetectChange.NOTE_ARRAY_CREATED_DATE];
			
		}
		return noteLUIDArray;
	}

	public static long[] getModLocalIdArray(long[][] modArray) {
		// TODO Auto-generated method stub
		long[] LUIDArray = getSingleDimenArray(modArray, AsyncDetectChange.MOD_ARRAY_LUID);
		return getLocalIdArrayFromLUIDArray(LUIDArray);
	}

	public static long getLocalIdFromLUID(long LUID) {
		// TODO Auto-generated method stub
		return (LUID-LUID%MILLI_SEC_FACTOR)/MILLI_SEC_FACTOR;
	}

	public static long getLUID(long localId, long createdDate) {
		
		return localId*MILLI_SEC_FACTOR+createdDate;
	}
	
	

}
