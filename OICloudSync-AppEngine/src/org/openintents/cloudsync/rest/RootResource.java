package org.openintents.cloudsync.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {
	@GET
	@Path("/logout")
	public String logout()
	{
		throw new RequestException(Response.Status.ACCEPTED);
	}
}
