package com.MWBFServer.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.Cache;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Utils 
{
	private static final Logger log = Logger.getLogger(Utils.class);
	
	/**
	 * Add users to the Database
	 */
	public static void addUsers()
	{
		User p1 = new User("arjunmuk@gmail.com", "password", "Arjun", "Mukherjee");
		User p2 = new User("sowmya.shantharam@gmail.com", "password", "Sowmya", "Shantharam");
		addUser(p1);
		addUser(p2);
	}
	
	public static Boolean addUser(User _user)
	{
		return DbConnection.saveObj(_user);
	}
	
	/**
	 * Log the users activities.
	 * Update the users personal stats.
	 * @param _userActivityList
	 * @return
	 */
	public static Boolean logActivity(List<UserActivity> _userActivityList)
	{
		return DbConnection.saveList(_userActivityList);
	}
	
	/**
	 * Load all the users from the Database into the validUsers hashSet.
	 * User lookup becomes fast.
	 */
	public static void loadUsers(Set<User> _validUsers, Map<String,User> _existingUsers)
	{
		List<User> userList = (List<User>) DbConnection.queryGetUsers();
		for (User user : userList)
		{
			if ( _validUsers != null )
				_validUsers.add(user);
			
			_existingUsers.put(user.getEmail(),user);
		}
	}
	
	/**
	 * Lookup up all the activities for the user.
	 * Aggregate it by Activity
	 */
	public static List<UserActivity> getUserActivitiesByActivityForDateRange(User _user, String _fromDate, String _toDate)
	{
		Date fromDate = null,toDate = null;
		try 
		{
			fromDate = new SimpleDateFormat("MMMM d, yyyy K:m:s a", Locale.ENGLISH).parse(_fromDate);
			toDate = new SimpleDateFormat("MMMM d, yyyy K:m:s a", Locale.ENGLISH).parse(_toDate);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		
		List<UserActivity> activityList =  (List<UserActivity>) DbConnection.queryGetUserActivity(_user);
		Map<String,UserActivity> activityHash = new HashMap<String,UserActivity>();
		
		// Aggregate the activities (units and points)
		for (UserActivity ua : activityList)
		{
			// TODO : Very inefficient goes through the whole list to figure out correct date range.
			// Ensure the activity is within the specified date
			if ( ua.getDate().after(fromDate) && ua.getDate().before(toDate) )
			{
				String activity = ua.getActivityId();
				if (activityHash.containsKey(activity))
				{
					UserActivity uaTemp = activityHash.get(activity);
					uaTemp.setExerciseUnits(ua.getExerciseUnits() + uaTemp.getExerciseUnits());
					uaTemp.setPoints(ua.getPoints() + uaTemp.getPoints());
					activityHash.put(activity, uaTemp);
				}
				else
					activityHash.put(activity, ua);
			}
		}
		
		List<UserActivity> returnList = new ArrayList<UserActivity>();
		for (UserActivity ua : activityHash.values())
			returnList.add(ua);
			
		return returnList;
	}
	
	/**
	 * Lookup up all the activities for the user.
	 * Aggregate it by Time
	 */
	public static List<UserActivityByTime> getUserActivitiesByTimeForDateRange(User _user, String _fromDate, String _toDate)
	{
		// TODO : Fix this, not good to parse the string to construct the object
		Date fromDate = null,toDate = null;
		try 
		{
			fromDate = new SimpleDateFormat("MMMM d, yyyy K:m:s a", Locale.ENGLISH).parse(_fromDate);
			toDate = new SimpleDateFormat("MMMM d, yyyy K:m:s a", Locale.ENGLISH).parse(_toDate);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		
		String dateAggregatedBy = "month";
		long diffInMillies = toDate.getTime() - fromDate.getTime();
		if ( TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS) > 350 )
			dateAggregatedBy = "month";
		else if ( TimeUnit.HOURS.convert(diffInMillies,TimeUnit.MILLISECONDS) > 24  )
			dateAggregatedBy = "day";
		else 
			dateAggregatedBy = "hour";
		
		List<?> resultList = DbConnection.queryGetUserActivityByTime(_user, fromDate, toDate, dateAggregatedBy);
		Gson gson = new Gson();
		String returnStr = gson.toJson(resultList);
		
		JsonParser parser = new JsonParser();
	    JsonArray jArray = parser.parse(returnStr).getAsJsonArray();

	    List<UserActivityByTime> returnList = new ArrayList<UserActivityByTime>();
		
	    for(JsonElement obj : jArray )
	    {
	    	String[] dateParts = obj.toString().split(",");
	    	String date = dateParts[0].substring(2);
	    	
	    	if (dateAggregatedBy.equals("month"))
	    		date = date.substring(0, 3);
	    	else if (dateAggregatedBy.equals("hour"))
	    	{
	    		date = dateParts[1].substring(6);
	    		date = date.substring(0, date.length()-1);
	    		
	    		String[] hourParts = date.split(":");
	    		String tempDate = hourParts[0]+hourParts[2].substring(2);
	    		date = tempDate;
	    	}
	    	
	    	Double points = Double.valueOf(dateParts[2].substring(0, dateParts[2].length()-1));
	    	UserActivityByTime uat = new UserActivityByTime(date, points);
	 
	    	returnList.add(uat);
	    }
	  
	   return returnList;
	}
	
	/**
	 * Get a list of all the valid activities
	 */
	public static List<Activities> getActivityList()
	{
		return (List<Activities>) DbConnection.queryGetActivityList();
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

	/**
	 * Add a set of activities to the hash map.
	 * Run --> Run Activity Object
	 * @param mActivitieshash
	 */
	public static void loadActivities(Map<String, Activities> mActivitieshash) 
	{
		List<Activities> activitiesList =  (List<Activities>) DbConnection.queryGetActivityList();
		for (Activities activity : activitiesList)
			mActivitieshash.put(activity.getActivityName(), activity);
	}
	
	/**
	 * Deletes all the activities for a given user.
	 * @param user
	 * @return Boolean
	 */
	public static Boolean deleteAllActivitiesForUser(User _user) 
	{
		return DbConnection.deleteAllActivitiesForUser(_user);
	}
	
	/**
	 * Get a list of all of the users friends.
	 * @param _user
	 * @return
	 */
	public static List<Friends> getUserFriendsList(User _user) 
	{
		return (List<Friends>) DbConnection.queryGetFriendsList(_user);
	}
	
	/**
	 * Add a friend to a user.
	 * @param _user
	 * @param _friend
	 * @return
	 */
	public static boolean addFriend(User _user, User _friend) 
	{
		Friends friendObj = new Friends(_user,_friend);
		Friends userObj = new Friends(_friend,_user);
		
		List<Friends> friendsList = new ArrayList<Friends>();
		friendsList.add(userObj);
		friendsList.add(friendObj);
		
		return DbConnection.saveList(friendsList);
	}

	/**
	 * Add a new challenge.
	 * @param _newChallenge
	 * @return
	 */
	public static boolean addChallenge(Challenge _newChallenge) 
	{
		return DbConnection.saveObj(_newChallenge);
	}
	
	/**
	 * Get a list of all the challenges the user is in
	 * @param _user
	 * @return
	 */
	public static List<DBReturnChallenge> getChallenges(User _user) 
	{
		// First get the id for the challenge from the playerSet
		// Use this id to lookup all players
		// Use this id to lookup all challenges
		List<?> challengeList = DbConnection.queryGetChallenges(_user);
		
		Gson gson = new Gson();
		String challengeStr = gson.toJson(challengeList);
		
		JsonParser parser = new JsonParser();
	    JsonArray jArray = parser.parse(challengeStr).getAsJsonArray();
	    
	    Map<String,DBReturnChallenge> challengeMap = new HashMap<String,DBReturnChallenge>();
		
	    String idList = "999999999999999";
	    for(JsonElement obj : jArray )
	    {
	    	String[] challengeParts = obj.toString().split(",");
	    	String id = challengeParts[0].substring(1);
	    	idList = id + "," + idList;
	    	
	    	String endDateStr = challengeParts[1].substring(1);
	    	String endYear = challengeParts[2].split(" ")[1].trim();
	    	endDateStr = endDateStr + "," + endYear;
	    	
	    	String startDateStr = challengeParts[4].substring(1);
	    	String startYear = challengeParts[5].split(" ")[1].trim();
	    	startDateStr = startDateStr + "," + startYear;
	    	
	    	String name = challengeParts[3].substring(1, challengeParts[3].length()-1);
	    	String creator = challengeParts[6].substring(1, challengeParts[6].length()-1);
	    	
	    	Date endDate = null;
	    	Date startDate = null;
			try 
			{
				startDate = new SimpleDateFormat("MMMM d,yyyy", Locale.ENGLISH).parse(startDateStr);
		    	endDate = new SimpleDateFormat("MMMM d,yyyy", Locale.ENGLISH).parse(endDateStr);
			} 
			catch (ParseException e)
			{
				e.printStackTrace();
			}
	    	
			DBReturnChallenge ch = new DBReturnChallenge(name,startDate,endDate,null,null);
			ch.setCreatorId(creator);
	    	challengeMap.put(id, ch);
	    }
	    
	    // Get the players for all the challenges
	    Map<String,HashSet<String>> playerMap = new HashMap<String,HashSet<String>>();
	 	List<?> playersList = DbConnection.queryGetChallengePlayers(idList);
	 	String playersStr = gson.toJson(playersList);
	 	jArray = parser.parse(playersStr).getAsJsonArray();
	 	for(JsonElement obj : jArray )
	    {
	 		String[] challengeParts = obj.toString().split(",");
	    	String id = challengeParts[0].substring(1);
	    	String userId = challengeParts[1].substring(1,challengeParts[1].length()-2);
	    	
	    	if (playerMap.containsKey(id))
	    		playerMap.get(id).add(userId);
	    	else
	    	{
	    		HashSet<String> userIdSet = new HashSet<String>();
	    		userIdSet.add(userId);
	    		playerMap.put(id,userIdSet);
	    	}
	    }
	 	
	 	// Get the activities for all the challenges
	    Map<String,HashSet<String>> activityMap = new HashMap<String,HashSet<String>>();
	 	List<?> activityList = DbConnection.queryGetChallengeActivities(idList);
	 	String activityStr = gson.toJson(activityList);
	 	jArray = parser.parse(activityStr).getAsJsonArray();
	 	for(JsonElement obj : jArray )
	    {
	 		String[] challengeParts = obj.toString().split(",");
	    	String id = challengeParts[0].substring(1);
	    	String activity = challengeParts[1].substring(1,challengeParts[1].length()-2);
	    	
	    	if (activityMap.containsKey(id))
	    		activityMap.get(id).add(activity);
	    	else
	    	{
	    		HashSet<String> activitySet = new HashSet<String>();
	    		activitySet.add(activity);
	    		activityMap.put(id,activitySet);
	    	}
	    }
	 	
	 	// Complete the challenge object
	 	// Add it to the return list
	 	List<DBReturnChallenge> returnList = new ArrayList<DBReturnChallenge>();
	 	for (String id : challengeMap.keySet())
	 	{
	 		DBReturnChallenge ch = challengeMap.get(id);
	 		ch.setId(Long.parseLong(id));
	 		ch.setPlayersPointsSet(constructPlayerPointsSet(playerMap.get(id),ch.getStartDate(),ch.getEndDate(),activityMap.get(id)));
	 		ch.setActivitySet(activityMap.get(id));

	 		returnList.add(ch);
	 	}
	 	
	 	// Construct the message board
	 	// Get the activities for the players between the given dates
	 	for (String id : challengeMap.keySet())
	 	{
	 		DBReturnChallenge ch = challengeMap.get(id);
	 		List<String> messageList = new ArrayList<String>();
	 		List<?> userActivityList = DbConnection.queryUserActivitiesPerChallenge(playerMap.get(id),activityMap.get(id),ch.getStartDate(),ch.getEndDate());
	 		String userActivityStr = gson.toJson(userActivityList);
		 	jArray = parser.parse(userActivityStr).getAsJsonArray();
		 	for(JsonElement obj : jArray )
		    {
		 		String[] activityParts = obj.toString().split(",");
		 		String activityId = activityParts[1].substring(1,activityParts[1].length()-1);
		 		String activityDate = activityParts[2].substring(1);
		 		String activityUnits = activityParts[4];
		 		String userId = activityParts[6].substring(1,activityParts[6].length()-2);
		 		
		 		User user = DataCache.m_usersHash.get(userId);
		 		StringBuilder actString = new StringBuilder();
		 		actString.append(user.getFirstName());
		 		actString.append(" ");
		 		actString.append(DataCache.m_activitiesHash.get(activityId).getPastVerb());
		 		actString.append(" ");
		 		actString.append(activityUnits);
		 		actString.append(" ");
		 		actString.append(DataCache.m_activitiesHash.get(activityId).getMeasurementUnit());
		 		actString.append(" on ");
		 		actString.append(activityDate);
		 		messageList.add(actString.toString());
		    }
		 	
		 	ch.setMessagesList(messageList);
	 	}
	 
	 	return returnList;
	}
	
	private static Set<String> constructPlayerPointsSet(Set<String> _players, Date _fromDate, Date _toDate, Set<String> _activitySet)
	{
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray jArray;
		Set<String> playerPointsMap = new HashSet<String>();
		
		// Construct the list of activities to sum the points by
		String activityList = null;
		for(String activity : _activitySet)
			activityList = "'" + activity + "'," + activityList; 
		
		for (String player : _players)
		{
			// Get the points for each player between the start and endDates
		 	List<?> userActivityList =  DbConnection.queryGetUserActivityByTime(player,_fromDate,_toDate, activityList);
		 	
		 	if (userActivityList == null || userActivityList.size() <= 0 )
		 		playerPointsMap.add(player + "," + "0");
		 	else
		 	{
		 	 	String activityStr = gson.toJson(userActivityList);
			 	
			 	jArray = parser.parse(activityStr).getAsJsonArray();
			 	for(JsonElement obj : jArray )
			 		playerPointsMap.add(player + "," + obj.toString());
			}
		}
		
		return playerPointsMap;
	}
	

	/**
	 * Get the personal stats for the user
	 * @param _user
	 * @return
	 */
	public static List<UserActivityByTime> getAllTimeHighs(User _user) 
	{
	    List<UserActivityByTime> returnListDay = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, "day"), "day");
	    
	    
	    UserActivityByTime bestDay;
	    UserActivityByTime bestMonth;
	    UserActivityByTime bestYear;
	    List<UserActivityByTime> returnList = new ArrayList<UserActivityByTime>();
	    
	    if ( (returnListDay != null) && (returnListDay.size() > 0) )
	    {
		    bestDay = returnListDay.get(0);
		    for (UserActivityByTime uat : returnListDay)
		    {
		    	if (uat.getPoints() > bestDay.getPoints())
		    		bestDay = uat;
		    }
		    List<UserActivityByTime> returnListMonth = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, "month"), "month");
		    bestMonth = returnListMonth.get(0);
		    for (UserActivityByTime uat : returnListMonth)
		    {
		    	if (uat.getPoints() > bestMonth.getPoints())
		    		bestMonth = uat;
		    }
		    
		    List<UserActivityByTime> returnListYear = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, "year"), "year");
		    bestYear = returnListYear.get(0);
		    for (UserActivityByTime uat : returnListYear)
		    {
		    	if (uat.getPoints() > bestYear.getPoints())
		    		bestYear = uat;
		    }
		    
		    returnList.add(bestDay);
		    returnList.add(bestMonth);
		    returnList.add(bestYear);
	    }
	    
		return returnList;
	}
	
	private static List<UserActivityByTime> convertToObjectArray(List<?> resultsList, String _dateAggregateBy)
	{
		List<UserActivityByTime> returnList = new ArrayList<UserActivityByTime>();
		
		Gson gson = new Gson();
		String returnStrDay = gson.toJson(resultsList);
		
		JsonParser parser = new JsonParser();
	    JsonArray jArray = parser.parse(returnStrDay).getAsJsonArray();
		
		for(JsonElement obj : jArray )
	    {
			String[] dateParts = obj.toString().split(",");
	    	String date = dateParts[0].substring(2);
	    	String year = dateParts[1].split(" ")[1].trim();
	    	
	    	if (_dateAggregateBy.equals("month"))
	    		date = date.substring(0, 3);
	    	else if (_dateAggregateBy.equals("year"))
	    		date = year;
	    	
	    	if (!_dateAggregateBy.equals("year"))
	    		date = date + "," + year;
	    	
	    	date.trim();
	    	
	    	Double points = Double.valueOf(dateParts[2].substring(0, dateParts[2].length()-1));
	    	points = round(points,1);
	    	UserActivityByTime uat = new UserActivityByTime(date, points);
	 
	    	returnList.add(uat);
	    }
		
		return returnList;
	}
	
	/**
	 * Round off a double value.
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places) 
	{
	    if (places < 0) 
	    	throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	/**
	 * First find the challenge object.
	 * Delete the challenge object.
	 * @param challengeId
	 * @return
	 */
	public static boolean deleteChallenge(String _challengeId) 
	{
		List<Challenge> challengeList = (List<Challenge>) DbConnection.queryGetChallenge(_challengeId);
		Challenge challenge = challengeList.get(0);
		
		boolean success = true;
		if (challenge != null )
			success = DbConnection.deleteChallenge(challenge);
		else
			success = false;
		
		return success;
	}

}
