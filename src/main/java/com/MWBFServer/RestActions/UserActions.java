package com.MWBFServer.RestActions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Dto.WeeklyComparisons;
import com.google.gson.JsonSyntaxException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.MWBFServer.Activity.*;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.LeaderActivityByTime;
import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Users.*;
import com.MWBFServer.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("/user")
public class UserActions
{
	private static final Logger log = Logger.getLogger(UserActions.class);
	private static final DataCache m_cache = DataCache.getInstance();
	
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
		String profileId = userData.optString("profileId").trim();
		
		log.info("FaceBook user login [" + email + "], FirstName[" + firstName + "], LastName[" + lastName + "], ProfileId[" + profileId + "]");
		
		//  First check if the user exists, if not, then register the user
		String returnStr = null;
		if ( (email == null) || (email.length() <= 1)  )
			returnStr =   "{\"success\":0,\"message\":\"Unable to login. Invalid email address obtained from Facebook!\"}";
		else
		{
			String name = firstName + " " + lastName;
			User user = m_cache.getUserById(email);
			if ( user != null ) 
				returnStr =   "{\"success\":1,\"message\":\"Welcome "+name+" !\"}";
			else
			{
				log.info("First time FaceBook User Registering [" + email + "]");
				User newUser = new User(email,firstName,lastName,profileId);
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
		
		//  First check if the user exists
		String returnStr = null;
		if ( (email == null) || (email.length() <= 1)  )
			returnStr =   "{\"success\":0,\"message\":\"Unable to get all time high for the user\"}";
		else
		{
			User user = m_cache.getUserById(email);
			if ( user != null ) 
			{
				// Get the users personal stats
				Gson gson = new Gson();
				 
				// Look up the users personal stats
				log.info("Getting the users all time high stats.");
				List<UserActivityByTime> allTimeHighList = Utils.getAllTimeHighs(user);
				if ( allTimeHighList != null && allTimeHighList.size() > 0 )
					returnStr = gson.toJson(allTimeHighList);
				
				// TODO : Add the stats in this call, so no need to make two rest calls
				//gson.toJson(Utils.getWeeklyStats(user));
			}
		}
		
		return Utils.buildResponse(returnStr);
	}
	
	
	@POST
	@Path("/leaderAllTimeHighs")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeaderStats(String _incomingData)
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
		
		//  First check if the user exists
		String returnStr = null;
		if ( (email == null) || (email.length() <= 1)  )
			returnStr =   "{\"success\":0,\"message\":\"V1 : Unable to get all time high for the user\"}";
		else
		{
			User user = m_cache.getUserById(email);
			if ( user != null ) 
			{
				Gson gson = new Gson();
				 
				// Look up the user's friends stats
				log.info("Getting the all time high stats for the user's friends.");
				List<LeaderActivityByTime> allTimeHighListFriends = Utils.getLeaderAllTimeHighs(user);
				if ( allTimeHighListFriends != null && allTimeHighListFriends.size() > 0 )
					returnStr = gson.toJson(allTimeHighListFriends);
				
				// TODO : Add the stats in this call, so no need to make two rest calls
				//gson.toJson(Utils.getWeeklyStats(user));
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
		if ( m_cache.getUserById(newUser.getEmail()) != null )
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
		if ( m_cache.getUserById(newUser.getEmail()) != null )
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
			m_cache.addUser(newUser);
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

		log.info("Logging user activities [" + _incomingData + "]");

		Type collectionType = new TypeToken<List<UserActivity>>(){}.getType();
		List<UserActivity> newActivityList = null;
        try 
        {
            newActivityList = gson.fromJson(_incomingData, collectionType);
        } 
        catch (JsonSyntaxException jse) 
        {
            log.error("Error logging user activity.", jse);
            returnStr =   "{\"success\":0,\"message\":\"Unable to log activity, please try again.\"}";
            return Utils.buildResponse(returnStr);
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
		
		User user = m_cache.getUserById(userData.optString("user_id"));
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
		
		User user = m_cache.getUserById(userData.optString("user_id"));
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
		
		String returnStr = null;
		User user = m_cache.getUserById(userData.optString("user_id"));
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
			String userId = userData.optString("user_id");
			User user = m_cache.getUserById(userId);
			if ( user == null )
			{
				log.warn("Unable to find logged in user (something's wrong) [" + userId + "].");
				returnStr = "{\"success\":0,\"message\":\"Unable to find logged in user (something's wrong).\"}";
			}
			else
			{
				log.info("Fetching all friends for UserId["+ user.getId() +"]");
				
				Gson gson = new Gson();
			 
				// Look up the users friends
				List<Friends> friendsList = m_cache.getFriends(user);
				
				// Null out the user Object and the password fields
				if ( friendsList != null )
				{
					for (Friends friendPair : friendsList)
						friendPair.setUser(null);
					
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
    @Path("/friends/feed")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserFeedItems(String _incomingData)
    {
        String returnStr = "{\"success\":0,\"message\":\"Unable to find your friends.\"}";
        JSONObject userData = null;
        try
        {
            userData = new JSONObject(_incomingData);
            String userId = userData.optString("user_id");
            User user = m_cache.getUserById(userId);
            if ( user == null )
            {
                log.warn("Unable to find logged in user (something's wrong) [" + userId + "].");
                returnStr = "{\"success\":0,\"message\":\"Unable to find logged in user (something's wrong).\"}";
            }
            else
            {
                log.info("Fetching all friends activities for UserId["+ user.getId() +"]");

                Gson gson = new Gson();

                // Look up the users friends
                List<Friends> friendsList = m_cache.getFriends(user);
                List<FeedItem> activityList = null;
                
                // Look up the friends activities
                if ( ( friendsList != null ) && ( friendsList.size() > 0 ) )
                	activityList = Utils.getUserFeedItems(friendsList, user);

                if ( activityList != null )
                    returnStr = gson.toJson(activityList);
                else
                    returnStr = "{\"success\":0,\"message\":\"Unable to find any activities for friends.\"}";
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return Utils.buildResponse(returnStr);
    }
    
    @POST
	@Path("/weeklyComparisons")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response compareUserEffortToFriends(String _incomingData)
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
			returnStr =   "{\"success\":0,\"message\":\"Unable to get weekly comparison stats for the user\"}";
		else
		{
			User user = m_cache.getUserById(email);
			if ( user != null ) 
			{
				// Get the users personal stats
				Gson gson = new Gson();
				 
				// Look up the users personal stats
				log.info("Getting the users weekly comparison stats.");
				WeeklyComparisons wk = Utils.getWeeklyStats(user);
				if ( wk != null  )
					returnStr = gson.toJson(wk);
			}
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
			
			User user = m_cache.getUserById(userData.optString("user_id"));
			User friend = m_cache.getUserById(userData.optString("friend_user_id"));
			if ( user == null || friend == null )
			{
				log.warn("Unable to find the user or the friend to add.");
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
			
			User user = m_cache.getUserById(userData.optString("user_id"));
			if ( user == null )
			{
				log.warn("Unable to find your friend [" + userData.optString("user_id") + "]. Please ask them to join us.");
				returnStr =   "{\"success\":0,\"message\":\"Unable to find your friend. Please ask them to join us.\"}";
			}
			else
			{
				log.info("Found friend with id[" + user.getId() + "].");
				
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
	@Path("/v1/findFriends")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response findFriendsV1(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to find your friend. Please ask them to join us..\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			
			String userIdentification = userData.optString("userIdentification");
			
			// First search by email
			// If user is not found search by first/last name
			if ( ( userIdentification != null ) && ( userIdentification.length() > 0 ) )
			{
				User user = m_cache.getUserById(userIdentification);
				if ( user == null )
				{
					List<User> usersList = m_cache.getUserByName(userIdentification);
					if ( ( usersList == null ) || ( usersList.size() < 1 ) )
					{
						log.warn("Unable to find your friend [" + userIdentification + "]. Please ask them to join us.");
						returnStr =   "{\"success\":0,\"message\":\"Unable to find your friend. Please ask them to join us.\"}";
					}
					else
					{
						log.info("Found friend with User Identification [" + userIdentification + "].");
						
						Gson gson = new Gson();
						returnStr = gson.toJson(usersList);
					}
				}
				else
				{
					List<User> usersList = new ArrayList<User>();
					usersList.add(user);
					log.info("Found friend with User Identification [" + userIdentification + "].");
					
					Gson gson = new Gson();
					returnStr = gson.toJson(usersList);
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
	@Path("/challenge/add")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addChallenge(String _incomingData)
	{
		String returnStr = null;
		Gson gson = new Gson();
		JSONObject userData;
		try 
		{
			userData = new JSONObject(_incomingData);
			User user = m_cache.getUserById(userData.optString("user_id"));
			
			Challenge newChallenge = gson.fromJson(_incomingData, Challenge.class);
			newChallenge.setCreator(user);
			log.info("Adding new challenge [" + newChallenge.toString() + "]");
			
			// If successful, add to the local cache
			if ( Utils.addChallenge(newChallenge) )
				returnStr =   "{\"success\":1,\"message\":\"New challenge added.\"}";
			else
			{
				log.warn("Unable to add challenge, please try again.");
				returnStr =   "{\"success\":0,\"message\":\"Unable to add challenge, please try again.\"}";
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
			log.warn("Unable to add challenge, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to add challenge, please try again.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}

	@POST
	@Path("/challenge/delete")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteChallenge(String _incomingData)
	{
		String returnStr = null;
		JSONObject userData;
		try 
		{
			userData = new JSONObject(_incomingData);
			String challengeId = userData.optString("challenge_id");
			
			log.info("Deleting  challenge [" + challengeId + "]");
			
			// If successful, add to the local cache
			if ( Utils.deleteChallenge(challengeId) )
				returnStr =   "{\"success\":1,\"message\":\"Challenge deleted.\"}";
			else
			{
				log.warn("Unable to delete challenge, please try again.");
				returnStr =   "{\"success\":0,\"message\":\"Unable to delete challenge, please try again.\"}";
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
			log.warn("Unable to delete challenge, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to delete challenge, please try again.\"}";
		}
		
		return Utils.buildResponse(returnStr);
	}

	@POST
	@Path("/challenge/getAll")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllChallenges(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to find your challenges.\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			User user = m_cache.getUserById(userData.optString("user_id"));
			if ( user == null )
			{
				log.warn("Unable to find user.");
				returnStr = "{\"success\":0,\"message\":\"Unable to find logged in user (something's wrong).\"}";
			}
			else
			{
				log.info("Fetching all challenges for UserId["+ user.getId() +"]");
				
				Gson gson = new Gson();
			 
				// Look up the users challenges
				List<DBReturnChallenge> challengeList = Utils.getChallengesV1(user);
				
				// Convert the List to a Json representation
				if ( challengeList != null )
					returnStr = gson.toJson(challengeList);
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

