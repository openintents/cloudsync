package org.openintents.cloudsync.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openintents.cloudsync.server.OICloudSyncService;
import org.openintents.cloudsync.server.DataStore;
import org.openintents.cloudsync.server.Task;

import com.sun.jersey.api.NotFoundException;

@Path("/notes")
public class NotesResource {
	
	OICloudSyncService service = new OICloudSyncService();
	
	@GET
	@Path("/get")
	@Produces("application/json")
	public String get(@DefaultValue("-1") @QueryParam("_id") int id) {
		
		if (id == -1) { // No note _id specified, so fetch all notes
			List<Task> notes = service.queryTasks(Util.NOTEPAD_PACKAGE_NAME);

			// Combine all notes into one JSON string
			// TODO: Find a better way to combine all notes
			String ret = "[";
			boolean flag = true;

			for (Task note : notes) {

				String delim = ",";

				// Don't add delimiter before first element
				if (flag) {
					flag = false;
					delim = "";
				}

				// Add _id to the notepad note JSON so it can be identified by
				// the web interface
				ret = ret.concat(delim
						+ Util.appendJSON(note.getJsonStringData(), "_id",
								note.getId()));

			}

			ret = ret.concat("]");
			return ret;
		} else {
			Task task = service.readTask((long) id);
			if (task == null) {
				throw new NotFoundException();
			}
			return Util.appendJSON(task.getJsonStringData(), "_id",
					task.getId());
		}
	}
	
	@POST 
	@Path("/update")
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String update(@DefaultValue("") @FormParam("_id") String _id, 
			@DefaultValue("") @FormParam("title") String title,
			@DefaultValue("") @FormParam("note") String note)
	{
		
		if(_id.isEmpty()) {
			//throw new BadRequestException("Bad Request");
			//System.out.println("Bad Request");
			throw new NotFoundException();
		}
		
		long id = Long.parseLong(_id);
		long appEngineTime = service.getAppEngineTime();
		Task task = service.readTask(id);
		String t = Util.appendJSON(task.getJsonStringData(), "title", title);
		t = Util.appendJSON(t, "note", note);
		task.setTimestamp(appEngineTime);
		task.setJsonStringData(t);
		service.updateTask(task);
		
		return t;
	}
	
	@GET
	@Path("/delete")
	@Produces("application/json")
	public String delete(@DefaultValue("") @QueryParam("_id") String _id)
	{
		if(_id.isEmpty()) {
			throw new NotFoundException();
		}
		
		long id = Long.parseLong(_id);
		
		Task task = service.readTask(id);
		if(task == null)
			throw new NotFoundException();
		if(!task.getEmailAddress().equalsIgnoreCase(DataStore.getUserEmail()))
		{
			throw new RequestException(Response.Status.FORBIDDEN);
		}
		
		service.deleteTask(task);
		
		return "";
	}
	

	@POST
	@Path("/new")
	@Produces("application/json")
	public String newNote(@DefaultValue("") @FormParam("title") String title, 
			@DefaultValue("") @FormParam("note") String note)
	{
		if(title.isEmpty())
			throw new RequestException(Response.Status.BAD_REQUEST);
		
		Task task = service.createTask();
		Note n = new Note();
		n.title = title;
		n.note = note;
		long appEngineTime = service.getAppEngineTime();
		n.created_date = n.modified_date = appEngineTime;
		
		task.setAppPackageName(Util.NOTEPAD_PACKAGE_NAME);
		task.setJsonStringData(Util.toJSON(n));
		task.setTimestamp(appEngineTime);
		service.updateTask(task);
		return "";
	}
}
