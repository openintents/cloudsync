package org.openintents.cloudsync.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;

/** Thrown to return a 400 Bad Request response with a list of error messages in the body. */
public class BadRequestException extends WebApplicationException
{
    private static final long serialVersionUID = 1L;
    private List<String> errors;

    public BadRequestException(String... errors)
    {
        this(Arrays.asList(errors));
    }

    public BadRequestException(List<String> errors)
    {
        super(Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XHTML_XML)
                .entity(new GenericEntity<List<String>>(errors)
                {}).build());
        this.errors = errors;
    }
    
    public BadRequestException()
    {
    	super(Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XHTML_XML).build());
    }
    public List<String> getErrors()
    {
        return errors;
    }
}
