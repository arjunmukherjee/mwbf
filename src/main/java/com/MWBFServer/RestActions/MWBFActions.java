package com.MWBFServer.RestActions;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Utils.Utils;
import com.google.gson.Gson;


@Path("/mwbf")
public class MWBFActions 
{
	private static final Logger log = Logger.getLogger(MWBFActions.class);
	
	@POST
	@Path("/activities")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActivityListPost()
	{
		log.info("Fetching All MWBF Activities.");
		
		Gson gson = new Gson();
		String returnStr = gson.toJson(DataCache.getInstance().getActivities());
	
		return Utils.buildResponse(returnStr);
	}
	
	@GET
	@Path("/test")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response test()
	{
		String returnStr = null;
		log.info("TEST SERVICE.");
		
		returnStr =   "{\"success\":1,\"message\":\"Test successful.\"}";

		return Utils.buildResponse(returnStr);
	}
	
	
}
