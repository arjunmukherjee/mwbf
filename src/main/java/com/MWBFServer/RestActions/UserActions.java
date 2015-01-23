package com.MWBFServer.RestActions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Dto.FriendRequestsDto;
import com.MWBFServer.Dto.UserDto;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.MWBFServer.Activity.*;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.LeaderActivityByTime;
import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Notifications.Notifications;
import com.MWBFServer.Notifications.Notifications.ClientNotification;
import com.MWBFServer.Users.*;
import com.MWBFServer.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("/ver1/user")
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
	@Path("/emailLogin")
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
		
		User newUser = new User(userData.optString("email").trim(),
				WordUtils.capitalize(userData.optString("firstName").trim()),
				WordUtils.capitalize(userData.optString("lastName").trim()),
				"");
		
		log.info("ADDING USER : Email[" + newUser.getEmail() + "], FirstName[" + newUser.getFirstName() + "], LastName[" + newUser.getLastName() + "]");
		
		String returnStr = null;
		// First check if the email address is already registered
		if ( m_cache.getUserById(newUser.getEmail()) != null )
		{
			log.warn("NOPE : Email is already registered.");
			returnStr =   "{\"success\":0,\"message\":\"Email is already registered.\"}";
		}
		else
			// If successful, add to the local cache
			returnStr = addUser(newUser);
		
		return Utils.buildResponse(returnStr);
	}
	
	
	/**
	 * Add the user : persist in DB and save in cache.
	 * @param newUser
	 * @return
	 */
	private String addUser(User _newUser) 
	{
		String returnStr;
		// If successful, add to the local cache
		if ( Utils.addUser(_newUser) )
		{
			returnStr =   "{\"success\":1,\"message\":\"Welcome !\"}";
			m_cache.addUser(_newUser);
		}
		else
		{
			log.warn("Unable to register user, please try again.");
			returnStr =   "{\"success\":0,\"message\":\"Unable to register user, please try again.\"}";
		}
		
		return returnStr;
	}
	
	
	@POST
	@Path("/userInfo")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo(String _incomingData)
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
				// Look up the users personal stats
				log.info("Getting the users [" + email + "] personal stats.");
				UserDto userDtoObj = Utils.getUserInfo(user);
				userDtoObj.setWeeklyComparisons(Utils.getWeeklyStats(user));
				
				Gson gson = new Gson();
				returnStr = gson.toJson(userDtoObj);
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
			}
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
	@Path("/deleteUserActivity")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteActivity(String _incomingData)
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
		String userId = userData.optString("userId");
		String activityId = userData.optString("activityId");
		
		log.info("Deleting activity [" + activityId + "]  for user [" + userId + "]");
		
		String returnStr = null;
		User user = m_cache.getUserById(userId);
		if ( user == null )
		{
			log.warn("Unable to find user to look up activity");
			returnStr =   "{\"success\":0,\"message\":\"Unable to find user to look up activity.\"}";
		}
		else
		{
			// Delete all of the users activities
			if ( !Utils.deleteActivity(activityId) )
				returnStr = "{\"success\":0,\"message\":\"Unable to delete user activity.\"}";
			else
				returnStr =   "{\"success\":1,\"message\":\"Deleted the users activity.\"}";
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
				List<User> friendsList = m_cache.getFriends(user);
				
				List<UserDto> friendsDtoList = null;
				
				// 1. Null out the user Object and the password fields
				// 2. Get the stats for each friend
				// 3. Package into the DTO class
				if ( friendsList != null && friendsList.size() > 0 )
				{
					friendsDtoList = new ArrayList<UserDto>();
					
					for (User friend : friendsList)
						friendsDtoList.add(Utils.getUserInfo(friend));
					
					returnStr = gson.toJson(friendsDtoList);
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
                List<User> friendsList = m_cache.getFriends(user);
                List<FeedItem> activityList = null;
                
                // Look up the friends activities
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
	@Path("/friends/add")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response addFriend(String _incomingData)
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
				log.info("Creating request for : Friend ["+ friend.getId() +"], User[" + user.getId() + "]");
				
				// Add the friend request
				// TODO : Check if it is a duplicate friend request
				if ( Utils.addFriendRequest(user,friend) )
					returnStr =   "{\"success\":1,\"message\":\"Friend request added.\"}";
				else
				{
					log.warn("Unable to add friend request, please try again.");
					returnStr = "{\"success\":0,\"message\":\"Unable to add friend request, please try again.\"}";
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
	@Path("/friends/pendingRequests")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFriendRequests(String _incomingData)
	{
    	String returnStr = "{\"success\":0,\"message\":\"Unable to find your pending friend requests.\"}";
		
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
				log.info("Fetching all friend requests for UserId["+ user.getId() +"]");
				
				Gson gson = new Gson();
			 
				// Look up the users pending friend requests
				List<PendingFriendRequest> friendRequestList = Utils.getFriendRequests(user);
				
				// Find the friend and send back the friend obj
				// Convert the List to a Json representation
				if ( friendRequestList != null )
				{
					List<FriendRequestsDto> friendRequestsDtoList = new ArrayList<FriendRequestsDto>();
					for (PendingFriendRequest request : friendRequestList)
					{
						User userFromReq = m_cache.getUserById(request.getUserId());
						UserDto friendDto = Utils.getUserInfo(userFromReq);
						FriendRequestsDto reqDto = new FriendRequestsDto(request.getUserId(), friendDto, request.getId());
						friendRequestsDtoList.add(reqDto);
					}
					returnStr = gson.toJson(friendRequestsDtoList);
				}
				else
					returnStr = "{\"success\":1,\"message\":\"No friend requests found.\"}";
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	
		return Utils.buildResponse(returnStr);
	}
    
    @POST
	@Path("/friends/actionRequest")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response acceptFriendRequest(String _incomingData)
	{
		String returnStr = "{\"success\":0,\"message\":\"Unable to action friend request, please try again.\"}";
		
		JSONObject userData = null;
		try 
		{
			userData = new JSONObject(_incomingData);
			
			String friendRequestId = userData.optString("friend_request_id");
			String requestAction = userData.optString("friend_request_action");
			if ( friendRequestId == null || requestAction == null )
			{
				log.warn("Unable to find the pending friend request for id [null].");
				returnStr = "{\"success\":0,\"message\":\"Invalid friend request Id.\"}";
			}
			else
			{
				log.info("Actioning [" + requestAction + "] friend request with id ["+ friendRequestId +"]");
				
				// Accept the friend request
				if ( requestAction.equalsIgnoreCase("Accept") )
				{
					if ( Utils.acceptFriendRequest(friendRequestId) )
						returnStr =   "{\"success\":1,\"message\":\"Friend request accepted.\"}";
					else
					{
						log.warn("Unable to accept friend request, please try again.");
						returnStr = "{\"success\":0,\"message\":\"Unable to accept friend request, please try again.\"}";
					}
				}
				else
				{
					if ( Utils.rejectFriendRequest(friendRequestId) )
						returnStr =   "{\"success\":1,\"message\":\"Friend request rejected.\"}";
					else
					{
						log.warn("Unable to reject friend request, please try again.");
						returnStr = "{\"success\":0,\"message\":\"Unable to reject friend request, please try again.\"}";
					}
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
	@Path("/friends/find")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response findFriends(String _incomingData)
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
				userIdentification = userIdentification.toLowerCase();
				
				User user = m_cache.getUserById(userIdentification);
				if ( user == null )
				{
					// TODO : Exclude the user requesting the search from the results
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
				List<DBReturnChallenge> challengeList = Utils.getChallenges(user);
				
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
	
	@POST
	@Path("/notifications")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_FORM_URLENCODED})
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkNotifications(String _incomingData)
	{
		String returnStr = null;
		returnStr =   "{\"success\":1,\"message\":\"Checked user notifications.\"}";
		
		log.info("Checking user notifications.");

		Type collectionType = new TypeToken<List<ClientNotification>>(){}.getType();
		List<ClientNotification> notificationsList = null;
        try 
        {
        	Gson gson = new Gson();
        	notificationsList = gson.fromJson(_incomingData, collectionType);
        	
        	// Process the notifications
        	// 1. Lookup the friend 
        	// 2. Get their memberSince date
        	// 3. Compare their memberSince date with that of the user
        	// 4. If after , then check if the notification message has been sent
        	// 5. If sent, do nothing
        	// 6. If not sent, add to send list and mark as sent, save in db & cache
        	// 7. If before, discard
        	if ( ( notificationsList != null ) && ( notificationsList.size() > 0 ) )
        	{
        		User user = m_cache.getUserById(notificationsList.get(0).userId);
        		if ( user != null )
        		{
        			List<Notifications> userNotificationList = m_cache.getUserNotifications(user);
        			List<Notifications> notificationsReturnList = null;
	        		for (ClientNotification cn : notificationsList)
	        		{
	            		User friend = m_cache.getUserByFbId(cn.fbProfileId);
	            		if ( friend != null)
	            		{
	            			if ( friend.getMemberSince().after(user.getMemberSince()) )
	            			{
	            				Notifications not = new Notifications(user,cn);
	            				boolean notifyUser = true;
	            				if ( userNotificationList != null && userNotificationList.size() > 0 )
	            				{
	            					if ( userNotificationList.contains(not) )
	            						notifyUser = false;
	            				}
	            				
	            				if ( notifyUser )
	            				{
	            					if (notificationsReturnList == null)
            							notificationsReturnList = new ArrayList<Notifications>();
            						
            						// Save in cache
            						//m_cache.addNotification(not);
            						
            						// Save in Db
            						//Utils.saveObj(not);
            						
            						// Create new object to return to user
            						// Set the user field to the friend object
            						// That way the receiving client can identify who the new friend who joined was
            						Notifications returnNot = new Notifications(friend,not.getNotificationMessage());
            						notificationsReturnList.add(returnNot);
            					}
	            			}
	            		}
	        		}
	        		
	        		// Convert the List to a Json representation
					if ( ( notificationsReturnList != null) && ( notificationsReturnList.size() > 0 ) )
						returnStr = gson.toJson(notificationsReturnList);
        		}
        	}
        } 
        catch (JsonSyntaxException jse) 
        {
            log.error("Error processing notificaitons from user.", jse);
            returnStr =   "{\"success\":0,\"message\":\"Unable to process notifications, please try again.\"}";
            return Utils.buildResponse(returnStr);
        }
		
        return Utils.buildResponse(returnStr);
	}
}

