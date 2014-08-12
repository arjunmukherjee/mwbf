package com.MWBFServer.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Users.User;

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
			_validUsers.add(user);
			_existingUsers.put(user.getEmail(),user);
		}
	}
	
	/**
	 * Lookup up all the activities for the user.
	 */
	public static List<UserActivity> getUserActivityForDateRange(User _user, String _fromDate, String _toDate)
	{
		List<UserActivity> activityList =  (List<UserActivity>) DbConnection.queryGetUserActivity(_user);
		Map<String,UserActivity> activityHash = new HashMap<String,UserActivity>();
		
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
			//log.info("");
		
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
		{
			mActivitieshash.put(activity.getActivityName(), activity);
		}
	}

}