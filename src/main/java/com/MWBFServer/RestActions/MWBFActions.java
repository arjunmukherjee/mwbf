package com.MWBFServer.RestActions;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Utils.Utils;
import com.google.gson.Gson;

@Path("/mwbf")
public class MWBFActions 
{
	private static final Logger log = Logger.getLogger(MWBFActions.class);
	
	@GET
	@Path("/activities")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActivityList()
	{
		String returnStr = null;
		log.info("Fetching Activity List.");
		
		Gson gson = new Gson();

		// Look up the user activities
		List<Activities> activityList = Utils.getActivityList();
		if ( activityList != null )
			returnStr = gson.toJson(activityList);
		else
			returnStr =   "{\"success\":0,\"message\":\"Unable to get list of activities.\"}";

		return Utils.buildResponse(returnStr);
	}
	
	@GET
	@Path("/test")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response test()
	{
		String returnStr = null;
		log.info("Test Test.");
		
	
		returnStr =   "{\"success\":1,\"message\":\"Test successful.\"}";

		return Utils.buildResponse(returnStr);
	}
	
	
}
