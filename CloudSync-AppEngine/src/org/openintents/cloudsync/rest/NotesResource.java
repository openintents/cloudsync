package org.openintents.cloudsync.rest;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.openintents.cloudsync.server.DataStore;
import org.openintents.cloudsync.server.Task;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.NotFoundException;

@Path("/notes")
public class NotesResource {
	@GET
	@Path("/get")
	@Produces("application/json")
	public String get(@DefaultValue("-1") @QueryParam("_id") int id) {

		DataStore ds = new DataStore();

		if (id == -1) { // No note _id specified, so fetch all notes
			List<Task> notes = ds.findAll("org.openintents.notepad");

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
			Task task = ds.find((long) id);
			if (task == null) {
				throw new NotFoundException();
			}
			return Util.appendJSON(task.getJsonStringData(), "_id",
					task.getId());
		}
	}

	@POST @Path("/update")
	@Produces("application/json")
	public String update(@DefaultValue("-1") @QueryParam("_id") int id)
	{
		if(id == -1)
			throw new BadRequestException("Bad Request");
		else
			return "Updating";
	}
}
