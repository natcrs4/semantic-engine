package com.crs4.sem.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ForbiddenStatusException  extends WebApplicationException{

	 public ForbiddenStatusException(String message) {
         super(Response.status(Response.Status.FORBIDDEN)
             .entity(message).type(MediaType.TEXT_PLAIN).build());
     }
}
