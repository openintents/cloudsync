package org.openintents.cloudsync.util;

import org.openintents.cloudsync.util.RecievedData;


public class SyncUtil {
	
	/**
	 * This method is responsible for returning the JSON String associated with the localId.
	 * Here both the localId and JSON string come from the local client.
	 * @param id It is the local id from the client
	 * @param rdArray It is the array made of Id and JSON string associated with it.
	 * @return the string corresponding to the localID.
	 */
	public static String getJsonFromRDarray(Long id, RecievedData[] rdArray) {

		for(RecievedData rd : rdArray) {
			if(rd.getLocal_id() == id ) {
				return rd.getJsonString();
				
			}
		}
		return null;
	}
	
	/**
	 * This function takes a JSON string and maps the corresponding LocalID in rdArray.
	 * @param jsonString String which is used for mapping
	 * @param rdArray Array from the client
	 * @return mapped LocalId
	 */
	public static long getIdFromRDarray(String jsonString, RecievedData[] rdArray) {
		for(RecievedData rd: rdArray) {
			if(rd.getJsonString().equals(jsonString)) {
				return rd.getLocal_id();
			}
		}
		return -1;
	}
	
	/**
	 * This method is used to find a element in a double dimension array. Target is the value to be found and
	 * tagetCol is the column in array where it is. returnCol is the column whose element is returned when the same
	 * rows targetcol value matcheds the target.
	 * @param matrix
	 * @param matrixLength Is included to be on the safer side.
	 * @param target Value To be found
	 * @param targetCol Col in which the target exists.
	 * @param returnCol Col whose element is to be returned.
	 * @return
	 */
	public static long mapTheMatrix(long[][] matrix,int matrixLength, long target, int targetCol, int returnCol) {
		for(int i=0;i<matrixLength;i++) {
			if( matrix[i][targetCol] == target) {
				return matrix[i][returnCol];
			}
		}
		return -1;
	}
	
	public static StringBuilder addToJson(StringBuilder jsonBuilder, long localId, long gId, String jsonString) {
		
		String jsonArrElement = " { \"id\": \" "+localId+" \" , " + " \"jsonString\":  "+jsonString+"  , "+  " \"googleId\": \" "+ gId+" \" } " ;
		jsonBuilder.append(jsonArrElement);
		jsonBuilder.append(",");
	    return jsonBuilder;      
	}

}
