package com.example.services;

import java.util.TimeZone;

import com.example.models.Time;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

@Path("/time")
@Produces(MediaType.APPLICATION_JSON)
public class TimeService 
{
	private static final Logger log = Logger.getLogger(TimeService.class);
	
    @GET
    public Response get() 
    {
    	log.info("Time get()");
        //return new Time();
        
        String returnStr =   "{\"success\":0,\"message\":\"Unable to find activity for user.\"}";
        return buildResponse(returnStr);
    }
    
    @GET
    @Path("/{timezone}")
    public Time get(@PathParam("timezone") String timezone) 
    {
    	log.info("Timezone get()");
        return new Time(TimeZone.getTimeZone(timezone.toUpperCase()));
    }
    
    /**
	 * Build an HttpResponse string to be sent back to the requester.
	 * @param _responseString
	 * @return
	 */
	public static Response buildResponse(String _responseString)
	{
		Response rb = Response.ok(_responseString).build();
		return rb;
	}

}

