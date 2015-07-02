package com.MWBFServer.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Dto.WeeklyComparisons;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Datasource.DBReturnClasses.LeaderActivityByTime;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Users.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Utils 
{
	private static final CacheManager m_cache = BasicUtils.getCache();
	
	
	/**
	 * Private constructor. This class is not to be instantiated.
	 */
	private Utils()
	{
        throw new IllegalStateException( "Do not instantiate this class." );
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
		
		List<UserActivity> returnList = Collections.emptyList();
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
	    
	    List<User> friendList = m_cache.getFriends(_user);
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
	    	for (User friend : friendList)
	    	{
	    		List<UserActivityByTime> friendActivitiesByDay = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend, TimeAggregateBy.day), TimeAggregateBy.day);
	    		if ( (friendActivitiesByDay != null) && (friendActivitiesByDay.size() > 0) )
	    		{
	    			List<UserActivityByTime> friendActivitiesByWeek = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend, TimeAggregateBy.week), TimeAggregateBy.week);
	    			List<UserActivityByTime> friendActivitiesByMonth = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend, TimeAggregateBy.month), TimeAggregateBy.month);
	    			List<UserActivityByTime> friendActivitiesByYear = convertToObjectArray(DbConnection.queryGetAllTimeHighs(friend, TimeAggregateBy.year), TimeAggregateBy.year);

	    			UserActivityByTime friendBestDay = getBestUserAcitivityByPoints(friendActivitiesByDay);
	    			if (bestDayLeader == null)
	    			{
	    				bestDayLeader = friendBestDay;
	    				leaderDay = friend;
	    			}
	    			else
	    			{
	    				if (friendBestDay.getPoints() >= bestDayLeader.getPoints() )
	    				{
	    					bestDayLeader = friendBestDay;
	    					leaderDay = friend;
	    				}
	    			}

	    			UserActivityByTime friendBestWeek = getBestUserAcitivityByPoints(friendActivitiesByWeek);
	    			if (bestWeekLeader == null)
	    			{
	    				bestWeekLeader = friendBestWeek;
	    				leaderWeek = friend;
	    			}
	    			else
	    			{
	    				if (friendBestWeek.getPoints() >= bestWeekLeader.getPoints() )
	    				{
	    					bestWeekLeader = friendBestWeek;
	    					leaderWeek = friend;
	    				}
	    			}

	    			UserActivityByTime friendBestMonth = getBestUserAcitivityByPoints(friendActivitiesByMonth);
	    			if (bestMonthLeader == null)
	    			{
	    				bestMonthLeader = friendBestMonth;
	    				leaderMonth = friend;
	    			}
	    			else
	    			{
	    				if (friendBestMonth.getPoints() >= bestMonthLeader.getPoints() )
	    				{
	    					bestMonthLeader = friendBestMonth;
	    					leaderMonth = friend;
	    				}
	    			}

	    			UserActivityByTime friendBestYear = getBestUserAcitivityByPoints(friendActivitiesByYear);
	    			if (bestYearLeader == null)
	    			{
	    				bestYearLeader = friendBestYear;
	    				leaderYear = friend;
	    			}
	    			else
	    			{
	    				if (friendBestYear.getPoints() >= bestYearLeader.getPoints() )
	    				{
	    					bestYearLeader = friendBestYear;
	    					leaderYear = friend;
	    				}
	    			}
	    		}
	    		//else
	    			//log.info("No activities found for Friend ["+friend.getId()+"]");
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
	    	points = BasicUtils.round(points,1);
	    	UserActivityByTime uat = new UserActivityByTime(dateStr, points);
	 
	    	returnList.add(uat);
	    }
		
		return returnList;
	}
	
	
	/**
     * Get the friends activities for each user
     * @param _user
     * @return
     */
	public static List<FeedItem> getUserFeedItems(User _user)
    {
		return m_cache.getActivityFeed(_user);
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
		userPoints = BasicUtils.round(userPoints,1);
			
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
    	List<User> friendsList = m_cache.getFriends(_user);
    	if ( ( friendsList != null ) && ( friendsList.size() > 0 ) )
    	{
	    	for (User friend : friendsList)
	    	{
	    		Double friendPoints = Utils.getUsersPointsForCurrentTimeInterval(friend,TimeAggregateBy.week);
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
			userPoints = BasicUtils.round(userPoints,1);
			friendsPointsAverage = BasicUtils.round(friendsPointsAverage,1);
			leaderPoints = BasicUtils.round (leaderPoints,1);
			
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
	    		
	    		// Calculate the number of challenges won
	    		if ( challenge.getWinnerId() != null )
	    		{
	    			if ( m_cache.getUserById(challenge.getWinnerId()).equals(_user))
	    				numberOfChallengesWon++;
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
