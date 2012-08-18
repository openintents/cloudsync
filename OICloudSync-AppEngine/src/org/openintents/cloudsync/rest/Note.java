package org.openintents.cloudsync.rest;

public class Note {
	public String tags;
	public long created_date;
	public boolean encrypted;
	public String title;
	public int scroll_position;
	public long modified_date;
	public String theme;
	public int selection_start;
	public int selection_end;
	public String note;
	
	Note() {
		scroll_position = 0;
		tags = null;
		encrypted = false;
		theme = "";
	}
}
