package org.openintents.cloudsync.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {
	
	public static String NOTEPAD_PACKAGE_NAME = "org.openintents.notepad";
	public static String SHOPPINGLIST_PACKAGE_NAME = "org.openintents.shoppinglist";
	
	public static String appendJSON(String jsonString, String name, Long data)
	{
		JsonParser parser = new JsonParser();
		
		JsonObject object = parser.parse(jsonString).getAsJsonObject();
		
		object.addProperty(name, data);
		
		return object.toString();
	}
	
	public static String appendJSON(String jsonString, String name, String data)
	{
		JsonParser parser = new JsonParser();
		
		JsonObject object = parser.parse(jsonString).getAsJsonObject();
		
		object.addProperty(name, data);
		
		return object.toString();
	}
	
	public static String toJSON(Object note)
	{
		Gson gson = new Gson();
		return gson.toJson(note);		
	}
}
