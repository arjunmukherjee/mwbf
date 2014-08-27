package com.MWBFServer.RestActions;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
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

import com.MWBFServer.Activity.*;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Stats.PersonalStats;
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
		
		// Load all the MWBF activities into the cache
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
	@Path("/fbLogin")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateFBUser(String _incomingData)
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
		
		String email = userData.optString("email").trim();
		String firstName = userData.optString("firstName").trim();
		String lastName = userData.optString("lastName").trim();
		
		log.info("FaceBook user login [" + email + "], FirstName[" + firstName + "], LastName[" + lastName + "]");
		
		//  First check if the user exists, if not, then register the user
		String returnStr = null;
		if ( (email == null) || (email.length() <= 1)  )
			returnStr =   "{\"success\":0,\"message\":\"Unable to login. Invalid email address obtained from Facebook!\"}";
		else
		{
			String name = firstName + " " + lastName;
			User user = m_existingUsersHash.get(email);
			if ( user != null ) 
				returnStr =   "{\"success\":1,\"message\":\"Welcome "+name+" !\"}";
			else
			{
				log.info("First time FaceBook User Registering [" + email + "]");
				User newUser = new User(email,"",firstName,lastName);
				returnStr = addUser(newUser);
			}
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	
	@POST
	@Path("/allTimeHighs")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTimeHighs(String _incomingData)
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
		
		String email = userData.optString("user_id").trim();
		
		//  First check if the user exists, if not, then register the user
		String returnStr = null;
		if ( (email == null) || (email.length() <= 1)  )
			returnStr =   "{\"success\":0,\"message\":\"Unable to get all time high for the user\"}";
		else
		{
			User user = m_existingUsersHash.get(email);
			if ( user != null ) 
			{
				// Get the users personal stats
				Gson gson = new Gson();
				 
				// Look up the users personal stats
				log.info("Getting the users all time high stats.");
				List<UserActivityByTime> allTimeHighList = Utils.getAllTimeHighs(user);
				if ( allTimeHighList != null && allTimeHighList.size() > 0 )
					returnStr = gson.toJson(allTimeHighList);
			}
		}
		
		return Utils.buildResponse(returnStr);
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
		{
			log.warn("Email is already registered.");
			returnStr =   "{\"success\":0,\"message\":\"Email is already registered.\"}";
		}
		else
			// If successful, add to the local cache
			returnStr = addUser(newUser);
		
		return Utils.buildResponse(returnStr);
	}
	
	
	@POST
	@Path("/fbAdd")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addFBUser(String _incomingData)
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
		
		User newUser = new User(userData.optString("email"));
		
		log.info("ADDING FACEBOOK USER : Email[" + newUser.getEmail() + "]");
		
		String returnStr = null;
		// First check if the email address is already registered
		if ( m_existingUsersHash.containsKey(newUser.getEmail()) )
		{
			log.warn("Email is already registered.");
			returnStr =   "{\"success\":0,\"message\":\"Email is already registered.\"}";
		}
		else
			returnStr = addUser(newUser);
		
		return Utils.buildResponse(returnStr);
	}

	/**
	 * Add the user : persist in DB and save in cache.
	 * @param newUser
	 * @return
	 */
	private String addUser(User newUser) 
	{
		String returnStr;
		// If successful, add to the local cache
		if ( Utils.addUser(newUser) )
		{
			returnStr =   "{\"success\":1,\"message\":\"Welcome !\"}";
			m_validUsersSet.add(newUser);
			m_existingUsersHash.put(newUser.getEmail(),newUser);
		}
		else
		{
			log.warn("Unable to register user, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to register user, please try again.\"}";
		}
		
		return returnStr;
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
		{
			Double points = m_activitiesHash.get(ua.getActivityId()).getPointsPerUnit() * ua.getExerciseUnits();
			points = Utils.round(points, 2);
			ua.setPoints(points);
		}
		
		// If successful, add to the local cache
		if ( Utils.logActivity(newActivityList) )
			returnStr =   "{\"success\":1,\"message\":\"Activity logged.\"}";
		else
		{
			log.warn("Unable to log activity, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to log activity, please try again.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/activitiesByActivity")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserActivitiesByActivity(String _incomingData)
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

		String returnStr = null;
		String fromDate = userData.optString("from_date");
		String toDate = userData.optString("to_date");
		
		User user = m_existingUsersHash.get(userData.optString("user_id"));
		if ( user == null )
		{
			log.warn("Unable to find user to look up activity");
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			log.info("Fetching activities by activity : UserId["+ user.getId() +"], Email[" + user.getEmail() + "]");
			
			Gson gson = new Gson();
		 
			// Look up the users activities
			List<UserActivity> activityList = Utils.getUserActivitiesByActivityForDateRange(user, fromDate, toDate);
			if ( activityList != null )
				returnStr = gson.toJson(activityList);
			else
				returnStr =   "{\"success\":0,\"message\":\"Unable to find activity for user.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}

	@POST
	@Path("/activitiesByTime")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserActivitiesByTime(String _incomingData)
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

		String returnStr = null;
		String fromDate = userData.optString("from_date");
		String toDate = userData.optString("to_date");
		
		User user = m_existingUsersHash.get(userData.optString("user_id"));
		if ( user == null )
		{
			log.warn("Unable to find user to look up activity");
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			log.info("Fetching activities by time : UserId["+ user.getId() +"], Email[" + user.getEmail() + "]");
			
			Gson gson = new Gson();
		 
			// Look up the users activities, aggregated by time
			List<?> activityList = Utils.getUserActivitiesByTimeForDateRange(user, fromDate, toDate);
			if ( activityList != null )
				returnStr = gson.toJson(activityList);
			else
				returnStr =   "{\"success\":0,\"message\":\"Unable to find activity for user.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}

	@POST
	@Path("/deleteUserActivities")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetUserData(String _incomingData)
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
		log.info("Deleting all activities for user [" + userData.optString("user_id") + "]");
		
		User user = m_existingUsersHash.get(userData.optString("user_id"));
		
		String returnStr = null;
		if ( user == null )
		{
			log.warn("Unable to find user to look up activity");
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			// Delete all of the users activities
			if ( !Utils.deleteAllActivitiesForUser(user) )
				returnStr = "{\"success\":0,\"message\":\"Unable to delete user activity.\"}";
			else
				returnStr =   "{\"success\":1,\"message\":\"Deleted all of the users activities.\"}";
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
	
	@POST
	@Path("/friends")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserFriends(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to find your friends.\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			User user = m_existingUsersHash.get(userData.optString("user_id"));
			if ( user == null )
			{
				log.warn("Unable to find user.");
				returnStr = "{\"success\":0,\"message\":\"Unable to find logged in user (something's wrong).\"}";
			}
			else
			{
				log.info("Fetching all friends for UserId["+ user.getId() +"]");
				
				Gson gson = new Gson();
			 
				// Look up the users friends
				List<Friends> friendsList = Utils.getUserFriendsList(user);
				
				// Null out the user Object and the password fields
				if ( friendsList != null )
				{
					for (Friends friendPair : friendsList)
					{
						friendPair.setUser(null);
						friendPair.getFriend().setPassword("");
					}
				
					returnStr = gson.toJson(friendsList);
				}
				else
					returnStr = "{\"success\":0,\"message\":\"Unable to find your friends.\"}";
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/addFriend")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserFriends(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to add friend, please try again.\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			
			User user = m_existingUsersHash.get(userData.optString("user_id"));
			User friend = m_existingUsersHash.get(userData.optString("friend_user_id"));
			if ( user == null || friend == null )
			{
				log.warn("Unable to find user or friend to add.");
				returnStr = "{\"success\":0,\"message\":\"Unable to find the user or the friend in the system (something's wrong).\"}";
			}
			else
			{
				log.info("Adding friend ["+ friend.getId() +"], to User[" + user.getId() + "]");
				
				// If successful, add to the local cache
				if ( Utils.addFriend(user,friend) )
					returnStr =   "{\"success\":1,\"message\":\"Friend added.\"}";
				else
				{
					log.warn("Unable to add friend, please try again.");
					returnStr = "{\"success\":0,\"message\":\"Unable to add friend, please try again.\"}";
				}
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/findFriend")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response findFriend(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to find your friend. Please ask them to join us..\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			
			User user = m_existingUsersHash.get(userData.optString("user_id"));
			if ( user == null )
			{
				log.warn("Unable to find your friend [" + userData.optString("user_id") + "]. Please ask them to join us.");
				returnStr =   "{\"success\":0,\"message\":\"Unable to find your friend. Please ask them to join us.\"}";
			}
			else
			{
				log.warn("Found friend with id[" + user.getId() + "].");
				
				// No need to send back the password
				user.setPassword("");
				
				Gson gson = new Gson();
				returnStr = gson.toJson(user);
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	
		return Utils.buildResponse(returnStr);
	}
	
	
	@POST
	@Path("/challenge/add")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addChallenge(String _incomingData)
	{
		String returnStr = null;
		Gson gson = new Gson();
		Challenge newChallenge = gson.fromJson(_incomingData, Challenge.class);
		log.info("Adding new challenge [" + newChallenge.toString() + "]");
		
		// If successful, add to the local cache
		if ( Utils.addChallenge(newChallenge) )
			returnStr =   "{\"success\":1,\"message\":\"New challenge added.\"}";
		else
		{
			log.warn("Unable to add challenge, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to add challenge, please try again.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	@POST
	@Path("/challenge/getAll")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllChallenges(String _incomingData)
	{
		// TODO
		
		String returnStr = "{\"success\":0,\"message\":\"Unable to find your challenges.\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			User user = m_existingUsersHash.get(userData.optString("user_id"));
			if ( user == null )
			{
				log.warn("Unable to find user.");
				returnStr = "{\"success\":0,\"message\":\"Unable to find logged in user (something's wrong).\"}";
			}
			else
			{
				log.info("Fetching all challenges for UserId["+ user.getId() +"]");
				
				Gson gson = new Gson();
			 
				// Look up the users friends
				List<DBReturnChallenge> challengeList = Utils.getChallenges(user);
				
				// Convert the List to a Json representation
				if ( challengeList != null )
				{
					returnStr = gson.toJson(challengeList);
				}
				else
					returnStr = "{\"success\":0,\"message\":\"Unable to find your challenges.\"}";
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return Utils.buildResponse(returnStr);
	}
	
}

