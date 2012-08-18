package org.openintents.cloudsync.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;

/** Thrown to return a 400 Bad Request response with a list of error messages in the body. */
public class RequestException extends WebApplicationException
{
    private static final long serialVersionUID = 1L;
    private List<String> errors;

    public RequestException(String... errors)
    {
        this(Status.BAD_REQUEST, Arrays.asList(errors));
    }
    
    public RequestException(Response.Status code, String... errors)
    {
        this(code, Arrays.asList(errors));
    }

    public RequestException(List<String> errors)
    {
    	this(Status.BAD_REQUEST, errors);
    }
    
    public RequestException(Response.Status code, List<String> errors)
    {
        super(Response.status(code).type(MediaType.APPLICATION_JSON)
                .entity(new GenericEntity<List<String>>(errors)
                {}).build());
        this.errors = errors;
    }
    
    public RequestException()
    {
    	super(Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).build());
    }
    
    public RequestException(Response.Status code)
    {
    	super(Response.status(code).type(MediaType.APPLICATION_JSON).build());
    }
    
    public RequestException(int code)
    {
    	super(code);
    }
    
    public List<String> getErrors()
    {
        return errors;
    }
}
