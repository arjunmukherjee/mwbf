package com.MWBFServer.RestActions;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Users.*;
import com.MWBFServer.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("/user")
public class UserActions
{
	private static final Logger log = Logger.getLogger(UserActions.class);
	private static final Set<User> m_validUsersSet = new HashSet<User>();
	private static final Map<String,User> m_existingUsersHash = new HashMap<String,User>();
	private static final Map<String,Activities> m_activitiesHash = new HashMap<String,Activities>();
	
	static
	{
		// Load all the users into the cache
		Utils.loadUsers(m_validUsersSet, m_existingUsersHash);
		
		// Load all the activities into the cache
		Utils.loadActivities(m_activitiesHash);
	}
	
	
	@POST
	@Path("/login")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateUser(String _incomingData)
	{
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return validateUser(userData.optString("email"), userData.optString("password"));
	}
	
	@POST
	@Path("/add")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUser(String _incomingData)
	{
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		User newUser = new User(userData.optString("email"),
								userData.optString("password"),
								userData.optString("firstName"),
								userData.optString("lastName"));
		
		log.info("ADDING USER : Email[" + newUser.getEmail() + "], FirstName[" + newUser.getFirstName() + "], LastName[" + newUser.getLastName() + "]");
		
		String returnStr = null;
		// First check if the email address is already registered
		if ( m_existingUsersHash.containsKey(newUser.getEmail()) )
			returnStr =   "{\"success\":0,\"message\":\"Email is already registered.\"}";
		else
		{
			// If successful, add to the local cache
			if ( Utils.addUser(newUser) )
			{
				returnStr =   "{\"success\":1,\"message\":\"Welcome " + newUser.getFirstName() + " " + newUser.getLastName() + ".\"}";
				m_validUsersSet.add(newUser);
				m_existingUsersHash.put(newUser.getEmail(),newUser);
			}
			else
				returnStr =   "{\"success\":0,\"message\":\"Unable to register user, please try again.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/activity/log")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response logActivity(String _incomingData)
	{
		String returnStr = null;
		Gson gson = new Gson();
		
		Type collectionType = new TypeToken<List<UserActivity>>(){}.getType();
		List<UserActivity> newActivityList = gson.fromJson(_incomingData, collectionType);
		
		// Multiply the activity's points * the number of exercise units and then store in db
		for (UserActivity ua : newActivityList)
			ua.setPoints(m_activitiesHash.get(ua.getActivityId()).getPointsPerUnit() * ua.getExerciseUnits());
		
		// If successful, add to the local cache
		if ( Utils.logActivity(newActivityList) )
			returnStr =   "{\"success\":1,\"message\":\"Activity logged.\"}";
		else
			returnStr =   "{\"success\":0,\"message\":\"Unable to log activity, please try again.\"}";
		
		return Utils.buildResponse(returnStr);
	}
	
	@GET
	@Path("/activities")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserActivity(@QueryParam("user_id") String _userId)
	{
		String returnStr = null;
		User user = m_existingUsersHash.get(_userId);
		if ( user == null )
		{
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			log.info("Fetching Activity : UserId["+ user.getId() +"], Email[" + user.getEmail() + "]");
			
			Gson gson = new Gson();
		 
			// Look up the users activities
			List<UserActivity> activityList = Utils.getUserActivityForDateRange(user,null,null);
			if ( activityList != null )
				returnStr = gson.toJson(activityList);
			else
				returnStr =   "{\"success\":0,\"message\":\"Unable to find activity for user.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/activities")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserActivityPost(String _incomingData)
	{
		//log.info("Got message : [" + _incomingData + "]");
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		String returnStr = null;
		User user = m_existingUsersHash.get(userData.optString("user_id"));
		String fromDate = userData.optString("from_date");
		String toDate = userData.optString("to_date");
		if ( user == null )
		{
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			log.info("Fetching Activity : UserId["+ user.getId() +"], Email[" + user.getEmail() + "]");
			
			Gson gson = new Gson();
		 
			// Look up the users activities
			List<UserActivity> activityList = Utils.getUserActivityForDateRange(user, fromDate, toDate);
			if ( activityList != null )
				returnStr = gson.toJson(activityList);
			else
				returnStr =   "{\"success\":0,\"message\":\"Unable to find activity for user.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	private Response validateUser(String _email, String _password) 
	{
		String returnStr = null;
		log.info("Looking for User with Email : [" + _email + "]");
		
		User player = new User(_email,_password,"","");
		if ( !m_validUsersSet.contains(player) )
			returnStr =   "{\"success\":0,\"message\":\"Username and/or password is invalid.\"}";
		else
			returnStr = "{\"success\":1}";
		
		return Utils.buildResponse(returnStr);
	}
	
}

