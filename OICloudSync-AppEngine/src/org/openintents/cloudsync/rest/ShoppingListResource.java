package org.openintents.cloudsync.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.openintents.cloudsync.server.OICloudSyncService;


@Path("/shoppinglist")

/* TODO: For now this sends a 501 Not Implemented on each request
 		 Since OI Shopping List sync is not yet available */

public class ShoppingListResource {
	OICloudSyncService service = new OICloudSyncService();
	
	@GET
	@Path("/list/get")
	@Produces("application/json")
	public String listGet(@DefaultValue("-1") @QueryParam("id") long id) {
		throw new RequestException(501);
	}
	
	@GET
	@Path("/list/delete")
	@Produces("application/json")
	public String listDel(@DefaultValue("-1") @QueryParam("id") long id) {
		throw new RequestException(501);
	}
	
	@GET
	@Path("/list/new")
	@Produces("application/json")
	public String listNew(@QueryParam("name") String name)
	{
		throw new RequestException(501);
	}
	
	@GET
	@Path("/list/rename")
	@Produces("application/json")
	public String listRename(@DefaultValue("-1") @QueryParam("id") long id,
			@DefaultValue("") @QueryParam("oldname") String oldname, @QueryParam("newname") String newname)
	{
		throw new RequestException(501);
	}
	
	@GET
	@Path("/item/get")
	@Produces("application/json")
	public String itemGet(@DefaultValue("-1") @QueryParam("id") long id,
			@DefaultValue("-1") @QueryParam("list") long list)
	{
		throw new RequestException(501);
	}
	
	@POST
	@Path("/item/update")
	@Produces("application/json")
	public String itemUpdate(@DefaultValue("-1") @QueryParam("item_id") long id,
			@DefaultValue("") @QueryParam("item_name") String item_name,
			@DefaultValue("") @QueryParam("item_tags") String item_tags,
			@DefaultValue("-1") @QueryParam("item_price") double item_price,
			@DefaultValue("") @QueryParam("item_units") String item_units,
			@DefaultValue("-1") @QueryParam("list_id") long list_id,
			@DefaultValue("-1") @QueryParam("priority") long priority,
			@DefaultValue("-1") @QueryParam("quantity") long quantity,
			@DefaultValue("-1") @QueryParam("status") long status)
	{
		throw new RequestException(501);
	}
}
