package com.MWBFServer.RestActions;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.SimpleCache;
import com.MWBFServer.Utils.BasicUtils;
import com.MWBFServer.Utils.JsonConstants;
import com.google.gson.Gson;


@Path("/ver1/mwbf")
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
		String returnStr = gson.toJson(SimpleCache.getInstance().getMWBFActivities());
	
		return BasicUtils.buildResponse(returnStr);
	}
	
	@GET
	@Path("/test")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response test()
	{
		log.info("TEST SERVICE.");
		
		String returnStr = BasicUtils.constructReturnString(JsonConstants.SUCCESS_YES, "Test successful.");

		return BasicUtils.buildResponse(returnStr);
	}
	
	
}
