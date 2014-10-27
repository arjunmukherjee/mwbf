package com.MWBFServer.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Dto.UserDto;
import com.MWBFServer.Dto.WeeklyComparisons;
import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Datasource.DBReturnClasses.LeaderActivityByTime;
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
	private static final DataCache m_cache = DataCache.getInstance();
	
	/**
	 * Add a new user.
	 * @param _user
	 * @return
	 */
	public static Boolean addUser(User _user)
	{
		return DbConnection.saveObj(_user);
	}
	
	/**
	 * Log the users activities.
	 * @param _userActivityList
	 * @return
	 */
	public static Boolean logActivity(List<UserActivity> _userActivityList)
	{
		// Multiply the activity's points * the number of exercise units and then store in db
		for (UserActivity ua : _userActivityList)
		{
			Activities act = m_cache.getMWBFActivity(ua.getActivityId());
			// Will get used while persisting bonus activities
			if ( act != null )
			{
				Double points = act.getPointsPerUnit() * ua.getExerciseUnits();
				points = Utils.round(points, 1);
				ua.setPoints(points);
			}
		}

		// Save activity to DB
		boolean result = DbConnection.saveList(_userActivityList);
		
		// If DB save was successful, then save to local cache
		if ( result )
		{
			for (UserActivity ua : _userActivityList)
				m_cache.addUserActivity(ua);
		}
		
		return result;
	}
	
	/**
	 * For each user , get their individual info
	 * 1. Get the challenge stats
	 * 2. Get the Points stats
	 * @param _user
	 * @return UserDto object
	 */
	public static UserDto getUserInfo(User _user) 
	{
		List<UserActivityByTime> allTimeHighList = Utils.getAllTimeHighs(_user);
		
		List<Integer> challengeStatsList = Utils.getChallengesStatsForUser(_user);
		
		Double currentWeekPoints = Utils.getUsersPointsForCurrentTimeInterval(_user,TimeAggregateBy.week);
		Double currentMonthPoints = Utils.getUsersPointsForCurrentTimeInterval(_user,TimeAggregateBy.month);
		Double currentYearPoints = Utils.getUsersPointsForCurrentTimeInterval(_user,TimeAggregateBy.year);
		
		UserDto userDtoObj = null;
		if ( ( allTimeHighList != null ) && ( allTimeHighList.size() > 2 )  )
			userDtoObj = new UserDto(_user,currentWeekPoints,currentMonthPoints,currentYearPoints,challengeStatsList.get(0),challengeStatsList.get(1),challengeStatsList.get(2),allTimeHighList.get(0),allTimeHighList.get(1),allTimeHighList.get(2),allTimeHighList.get(3));
		else
		{
			UserActivityByTime emptyUserActivity = new UserActivityByTime("--", 0.0);
			userDtoObj = new UserDto(_user,currentWeekPoints,currentMonthPoints,currentYearPoints,challengeStatsList.get(0),challengeStatsList.get(1),challengeStatsList.get(2),emptyUserActivity,emptyUserActivity,emptyUserActivity,emptyUserActivity);
		}
		
		return userDtoObj;
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
		
		List<UserActivity> activityList =  m_cache.getUserActivities(_user);
		Map<String,UserActivity> activityHash = new HashMap<String,UserActivity>();
		
		List<UserActivity> returnList = null;
		// Aggregate the activities (units and points)
		if ( ( activityList != null ) && ( activityList.size() > 0 ) )
		{
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
			
			returnList = new ArrayList<UserActivity>();
			for (UserActivity ua : activityHash.values())
				returnList.add(ua);
		}
		
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
	 * Deletes all the activities for a given user.
	 * @param _user
	 * @return Boolean
	 */
	public static Boolean deleteAllActivitiesForUser(User _user) 
	{
		return DbConnection.deleteAllActivitiesForUser(_user);
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
		
		// Add the friend to the cache
		boolean result = DbConnection.saveList(friendsList);
		if ( result )
			m_cache.addFriend(_user, friendObj);
		
		return result;
	}

	/**
	 * Add a new challenge.
	 * @param _newChallenge
	 * @return
	 */
	public static boolean addChallenge(Challenge _newChallenge) 
	{
		boolean result = DbConnection.saveObj(_newChallenge);
		if ( result )
			m_cache.addChallenge(_newChallenge);
		
		return result;
	}
	
		
	/**
	 * Get a list of all the challenges the user is in
	 * @param _user
	 * @return
	 */
	public static List<DBReturnChallenge> getChallenges(User _user) 
	{
		// TODO : 
		// 1. Redundant code between feeds and this method
		// 2. Get activities from the cache
		Map<String,DBReturnChallenge> challengeMap = null;
		List<Challenge> challengeList = m_cache.getUserChallenges(_user);
		if ( ( challengeList != null ) && ( challengeList.size() > 0 ) )
		{

			challengeMap = new HashMap<String,DBReturnChallenge>();

			Gson gson = new Gson();
			JsonParser parser = new JsonParser();

			// Construct a unique map of the challengeReturn objects
			for(Challenge challenge : challengeList )
			{
				List<UserActivity> userActivityFeedList = new ArrayList<UserActivity>();

				DBReturnChallenge ch = new DBReturnChallenge(challenge.getName(),challenge.getStartDate(),challenge.getEndDate(),null,challenge.getActivitySet());
				ch.setCreatorId(challenge.getCreator().getId());
				ch.setPlayersPointsSet(constructPlayerPointsSet(challenge.getPlayersSet(),ch.getStartDate(),ch.getEndDate(),challenge.getActivitySet()));

				List<?> userActivityList = DbConnection.queryUserActivitiesPerChallenge(challenge.getPlayersSet(),challenge.getActivitySet(),ch.getStartDate(),ch.getEndDate());
				List<String> messageList = new ArrayList<String>();
				String userActivityStr = gson.toJson(userActivityList);
				JsonArray jArray = parser.parse(userActivityStr).getAsJsonArray();
				for(JsonElement obj : jArray )
				{
					String[] activityParts = obj.toString().split(",");
					String activityId = activityParts[1].substring(1,activityParts[1].length()-1);
					String activityDateStr = activityParts[2].substring(1);
					String activityUnits = activityParts[4];
					String userId = activityParts[6].substring(1,activityParts[6].length()-2);
					User user = m_cache.getUserById(userId);

					Date activityDate = null;
					try 
					{
						activityDate = new SimpleDateFormat("MMMM d", Locale.ENGLISH).parse(activityDateStr);
					} 
					catch (ParseException e)
					{
						e.printStackTrace();
					}

					UserActivity ua = new UserActivity( user,activityId,activityDate,activityUnits);

					// Get the users activity feeds
					userActivityFeedList.add(ua);
				}

				// Get the list of activities and sort them by time
				Collections.sort(userActivityFeedList);

				for (UserActivity ua : userActivityFeedList)
					messageList.add(ua.constructNotificationString());

				// Return only the last 50 items
				int startIndex = 0;
				int endIndex = Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;
				if( messageList.size() > Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
					startIndex = messageList.size() - Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;

				if( messageList.size() < Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
					endIndex = messageList.size();

				ch.setMessagesList(messageList.subList(startIndex, startIndex + endIndex));
				challengeMap.put(Long.toString(challenge.getId()), ch);
			}
		}

		if (challengeMap != null)
			return new ArrayList<DBReturnChallenge>(challengeMap.values());
		else
			return null;
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
	    List<UserActivityByTime> returnList = new ArrayList<UserActivityByTime>();
	    
	    List<UserActivityByTime> returnListDay = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, TimeAggregateBy.day), TimeAggregateBy.day);
	    if ( (returnListDay != null) && (returnListDay.size() > 0) )
	    {
		    List<UserActivityByTime> returnListWeek = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, TimeAggregateBy.week), TimeAggregateBy.week);
		    List<UserActivityByTime> returnListMonth = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, TimeAggregateBy.month), TimeAggregateBy.month);
		    List<UserActivityByTime> returnListYear = convertToObjectArray(DbConnection.queryGetAllTimeHighs(_user, TimeAggregateBy.year), TimeAggregateBy.year);
		    
		    returnList.add(getBestUserAcitivityByPoints(returnListDay));
		    returnList.add(getBestUserAcitivityByPoints(returnListWeek));
		    returnList.add(getBestUserAcitivityByPoints(returnListMonth));
		    returnList.add(getBestUserAcitivityByPoints(returnListYear));
	    }
	    
		return returnList;
	}
	
	/**
	 * Get the leader stats for the user's friends
	 * @param _user
	 * @return
	 */
	public static List<LeaderActivityByTime> getLeaderAllTimeHighs(User _user) 
	{
	    
	    List<LeaderActivityByTime> returnList = new ArrayList<LeaderActivityByTime>();
	    
	    List<Friends> friendList = m_cache.getFriends(_user);
	    UserActivityByTime bestDayLeader = null;
	    UserActivityByTime bestWeekLeader = null;
	    UserActivityByTime bestMonthLeader = null;
	    UserActivityByTime bestYearLeader = null;
	    User leaderDay = null;
	    User leaderWeek = null;
	    User leaderMonth = null;
	    User leaderYear = null;
	    if ( ( friendList != null ) && ( friendList.size() > 0 ) )
	    {
	    	for (Friends friend : friendList)
	    	{
	    		List<UserActivityByTime> friendActivitiesByDay = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend.getFriend(), TimeAggregateBy.day), TimeAggregateBy.day);
	    		if ( (friendActivitiesByDay != null) && (friendActivitiesByDay.size() > 0) )
	    		{
	    			List<UserActivityByTime> friendActivitiesByWeek = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend.getFriend(), TimeAggregateBy.week), TimeAggregateBy.week);
	    			List<UserActivityByTime> friendActivitiesByMonth = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend.getFriend(), TimeAggregateBy.month), TimeAggregateBy.month);
	    			List<UserActivityByTime> friendActivitiesByYear = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend.getFriend(), TimeAggregateBy.year), TimeAggregateBy.year);

	    			UserActivityByTime friendBestDay = getBestUserAcitivityByPoints(friendActivitiesByDay);
	    			if (bestDayLeader == null)
	    			{
	    				bestDayLeader = friendBestDay;
	    				leaderDay = friend.getFriend();
	    			}
	    			else
	    			{
	    				if (friendBestDay.getPoints() >= bestDayLeader.getPoints() )
	    				{
	    					bestDayLeader = friendBestDay;
	    					leaderDay = friend.getFriend();
	    				}
	    			}

	    			UserActivityByTime friendBestWeek = getBestUserAcitivityByPoints(friendActivitiesByWeek);
	    			if (bestWeekLeader == null)
	    			{
	    				bestWeekLeader = friendBestWeek;
	    				leaderWeek = friend.getFriend();
	    			}
	    			else
	    			{
	    				if (friendBestWeek.getPoints() >= bestWeekLeader.getPoints() )
	    				{
	    					bestWeekLeader = friendBestWeek;
	    					leaderWeek = friend.getFriend();
	    				}
	    			}

	    			UserActivityByTime friendBestMonth = getBestUserAcitivityByPoints(friendActivitiesByMonth);
	    			if (bestMonthLeader == null)
	    			{
	    				bestMonthLeader = friendBestMonth;
	    				leaderMonth = friend.getFriend();
	    			}
	    			else
	    			{
	    				if (friendBestMonth.getPoints() >= bestMonthLeader.getPoints() )
	    				{
	    					bestMonthLeader = friendBestMonth;
	    					leaderMonth = friend.getFriend();
	    				}
	    			}

	    			UserActivityByTime friendBestYear = getBestUserAcitivityByPoints(friendActivitiesByYear);
	    			if (bestYearLeader == null)
	    			{
	    				bestYearLeader = friendBestYear;
	    				leaderYear = friend.getFriend();
	    			}
	    			else
	    			{
	    				if (friendBestYear.getPoints() >= bestYearLeader.getPoints() )
	    				{
	    					bestYearLeader = friendBestYear;
	    					leaderYear = friend.getFriend();
	    				}
	    			}
	    		}
	    		else
	    			log.info("No activities found for Friend ["+friend.getFriend().getId()+"]");
	    	}

	    	LeaderActivityByTime dayLeaderObj = null;
	    	if (leaderDay != null )
	    	{
	    		dayLeaderObj = new LeaderActivityByTime(leaderDay,bestDayLeader.getDate(),bestDayLeader.getPoints(),TimeAggregateBy.day);
	    		returnList.add(dayLeaderObj);
	    	}

	    	LeaderActivityByTime weekLeaderObj = null;
	    	if (leaderWeek != null )
	    	{
	    		weekLeaderObj = new LeaderActivityByTime(leaderWeek,bestWeekLeader.getDate(),bestWeekLeader.getPoints(),TimeAggregateBy.week);
	    		returnList.add(weekLeaderObj);
	    	}

	    	LeaderActivityByTime monthLeaderObj = null;
	    	if (leaderMonth != null )
	    	{
	    		monthLeaderObj = new LeaderActivityByTime(leaderMonth,bestMonthLeader.getDate(),bestMonthLeader.getPoints(),TimeAggregateBy.month);
	    		returnList.add(monthLeaderObj);
	    	}

	    	LeaderActivityByTime yearLeaderObj = null;
	    	if (leaderYear != null )
	    	{
	    		yearLeaderObj = new LeaderActivityByTime(leaderYear,bestYearLeader.getDate(),bestYearLeader.getPoints(),TimeAggregateBy.year);
	    		returnList.add(yearLeaderObj);
	    	}
	    }
	    
	   return returnList;
	}
	
	private static UserActivityByTime getBestUserAcitivityByPoints(List<UserActivityByTime> _activityArray)
	{
		UserActivityByTime best = _activityArray.get(0);
		for (UserActivityByTime uat : _activityArray)
	    {
	    	if (uat.getPoints() > best.getPoints())
	    		best = uat;
	    }
		
		return best;
	}
	
	private static List<UserActivityByTime> convertToObjectArray(List<?> resultsList, TimeAggregateBy _aggUnit)
	{
		List<UserActivityByTime> returnList = new ArrayList<UserActivityByTime>();
		
		Gson gson = new Gson();
		String returnStrDay = gson.toJson(resultsList);
		
		JsonParser parser = new JsonParser();
	    JsonArray jArray = parser.parse(returnStrDay).getAsJsonArray();
		
	    for(JsonElement obj : jArray )
	    {
			String[] dateParts = obj.toString().split(",");
	    	String dateStr = dateParts[0].substring(2);
	    	String year = dateParts[1].split(" ")[1].trim();
	    	
	    	if ( _aggUnit == TimeAggregateBy.month )
	    		dateStr = dateStr.substring(0, 3);
	    	else if (_aggUnit == TimeAggregateBy.year)
	    		dateStr = year;
	    	
	    	if ( _aggUnit != TimeAggregateBy.year )
	    		dateStr = dateStr + "," + year;
	    	
	    	dateStr.trim();
	    	
	    	// If 'week' then add 5 days
	    	if (_aggUnit == TimeAggregateBy.week)
	    	{
	    		Date startDate = null;
				try 
				{
					// TODO : DB Week is Mon-Mon (Need to change that to Sun - Sat)
					startDate = new SimpleDateFormat("MMM d,yyyy", Locale.ENGLISH).parse(dateStr);
					DateFormat dateFormat= new SimpleDateFormat("MMM d,yyyy");

					Calendar c = Calendar.getInstance();    
					c.setTime(startDate);
					c.add(Calendar.DATE, 6);
					
					dateStr = dateStr + "-" + dateFormat.format(c.getTime());
			   } 
				catch (ParseException e)
				{
					e.printStackTrace();
				}
	    	}
	    	
	    	Double points = Double.valueOf(dateParts[2].substring(0, dateParts[2].length()-1));
	    	points = round(points,1);
	    	UserActivityByTime uat = new UserActivityByTime(dateStr, points);
	 
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
	 * @param _challengeId
	 * @return
	 */
	@SuppressWarnings("unchecked")
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


    /**
     * Get the friends activities for each user
     * @param friendsList
     * @param _user
     * @return
     */
	public static List<FeedItem> getUserFeedItems(List<Friends> friendsList, User _user)
    {
        // TODO : Highly inefficient

        List<UserActivity> activityList = new ArrayList<UserActivity>();
        for (Friends friend : friendsList)
        {
        	List<UserActivity> friendActivityList = m_cache.getUserActivities(friend.getFriend());
        	
        	if ( ( friendActivityList != null ) && ( friendActivityList.size() > 0 ) )
        		activityList.addAll(friendActivityList);
        }

        // Get the users activity feeds
        List<UserActivity> userActivityList = m_cache.getUserActivities(_user);
        activityList.addAll(userActivityList);

        // Get the list of activities and sort them by time
        Collections.sort(activityList);

        // Populate feed item list
        List<FeedItem> feedItemList = new ArrayList<FeedItem>();
        for (UserActivity activity : activityList) 
        {
            // Populate FeedItem object
            FeedItem item = new FeedItem();
            item.setActivityDate(activity.getDate());
            item.setActivityName(activity.getActivityId());
            
            if ( !activity.isBonusActivity() )
            	item.setActivityUnit(m_cache.getMWBFActivity(activity.getActivityId()).getMeasurementUnitShort());
            
            item.setActivityValue(activity.getExerciseUnits());
            item.setFirstName(activity.getUser().getFirstName());
            item.setLastName(activity.getUser().getLastName());
            item.setUserId(activity.getUser().getId());
            item.setPoints(activity.getPoints());
            item.setFeedPrettyString(activity.constructNotificationString());
            
            // Add to feedItemList
            feedItemList.add(item);
        }


        // Return only the last 50 items
        int startIndex = 0;
        int endIndex = Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;
        if( feedItemList.size() > Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
            startIndex = feedItemList.size() - Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;

        if( feedItemList.size() < Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
        	endIndex = feedItemList.size();
        
        return feedItemList.subList(startIndex, startIndex + endIndex);
    }

    /**
     * Get the users points for the timeInterval specified
     * @param _user
     * @param _timeInterval (week,month,year)
     * @return total points
     */
    public static Double getUsersPointsForCurrentTimeInterval (User _user, TimeAggregateBy _timeInterval)
    {
    	// Calculate the start and the end of the current week
    	Date date = new Date();
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	
    	SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
    	
    	String startDate = null;
    	String endDate = null;
    	int year = c.get(Calendar.YEAR);
    	
    	if ( _timeInterval == TimeAggregateBy.week )
    	{
    		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
        	
        	c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);

        	Date weekStart = c.getTime();
        	// we do not need the same day a week after, that's why use 6, not 7
        	c.add(Calendar.DAY_OF_MONTH, 6); 
        	Date weekEnd = c.getTime();
        	
    		startDate = df.format(weekStart);
    		endDate = df.format(weekEnd);
    	}
    	else if ( _timeInterval == TimeAggregateBy.month )
    	{
    		df = new SimpleDateFormat("MMM", Locale.ENGLISH);
    		String month = df.format(date);
    		startDate = month + " 01, " + year;
    		endDate = month + " 31, " + year;
    	}
    	else
    	{
    		startDate = "Jan 01, " + year ;
    		endDate = "Dec 31, " + year ;
    	}
    	
    	startDate = startDate + " 00:00:01 AM";
    	endDate = endDate + " 11:59:59 PM";
    	
    	// Get the users activity for the week
	    List<UserActivity> activityList = Utils.getUserActivitiesByActivityForDateRange(_user, startDate, endDate );
		Double userPoints = 0.0;
		if ( ( activityList != null ) && ( activityList.size() > 0 ) )
		{
			for (UserActivity ua : activityList)
				userPoints = userPoints + ua.getPoints();
		}
		
		// Round off the points to a single precision
		userPoints = round(userPoints,1);
			
		return userPoints;
    }
	
	
    /**
     * Get the stats (friends average, leader) for the week
     * @param _user
     * @return
     */
    public static WeeklyComparisons getWeeklyStats(User _user)
    {
    	Double friendsPointsTotal = 0.0;
    	Double leaderPoints = 0.0;
    	
    	// Get the friends activities for the week
    	WeeklyComparisons wkComp = null;
    	int activeFriendsCount = 0;
    	List<Friends> friendsList = m_cache.getFriends(_user);
    	if ( ( friendsList != null ) && ( friendsList.size() > 0 ) )
    	{
	    	for (Friends friend : friendsList)
	    	{
	    		Double friendPoints = Utils.getUsersPointsForCurrentTimeInterval(friend.getFriend(),TimeAggregateBy.week);
	    		if (friendPoints > leaderPoints)
		    		leaderPoints = friendPoints;
		    		
		    	friendsPointsTotal = friendsPointsTotal + friendPoints;
		    		
		    	// Look for active friends
		    	if ( friendPoints != 0.0 )
		    		activeFriendsCount++;
	    	}
	    	
	    	// Calculate the average across all the active friends
	    	Double friendsPointsAverage = friendsPointsTotal / activeFriendsCount;
			
	    	// Get the users points for the week
	    	Double userPoints = getUsersPointsForCurrentTimeInterval(_user,TimeAggregateBy.week);
				
			// Round off the points to a single precision
			userPoints = round(userPoints,1);
			friendsPointsAverage = round(friendsPointsAverage,1);
			leaderPoints = round (leaderPoints,1);
			
			wkComp = new WeeklyComparisons(userPoints, friendsPointsAverage, leaderPoints);
	    }
    	
		return wkComp;
    }
    
    /**
     * Returns the challenge stats for the user
     * 1. Total number of challenges
     * 2. Number of active challenges 
     * 3. Number of challenges won
     * @param _user
     * @return List of Integers in the above order
     */
    public static List<Integer> getChallengesStatsForUser(User _user) 
	{
    	// TODO : Complete implementation for challenges won
    	
		List<Challenge> challengeList = m_cache.getUserChallenges(_user);
		int totalNumberOfChallenges = 0;
    	int numberOfActiveChallenges = 0;
    	int numberOfChallengesWon = 0;
    	
    	Date today = new Date();
    	
    	if (challengeList != null && challengeList.size() > 0)
    	{
    		totalNumberOfChallenges = challengeList.size();
	    	for (Challenge challenge : challengeList)
	    	{
	    		// Check if the start date is in the past and the end date is in the future
	    		// TODO : if the start date is equal to today or endDate is equal to today
	    		if ( challenge.getStartDate().before(today) && challenge.getEndDate().after(today) )
	    			numberOfActiveChallenges++;
	    		
	    		// Check if the end date is in the past
	    		// TODO : if the end date is equal to today
	    		if ( challenge.getEndDate().before(today) )
	    		{
	    			// TODO : Challenges won
	    			// numberOfChallengesWon;
	    		}
	    	}
    	}
    	
    	// Construct the list to return
    	List<Integer> returnList = new ArrayList<Integer>();
    	returnList.add(totalNumberOfChallenges);
    	returnList.add(numberOfActiveChallenges);
    	returnList.add(numberOfChallengesWon);
    	
    	return returnList;
	}
    
    
	public static enum TimeAggregateBy
    {
    	hour,day,week,month,year;
    }
}
