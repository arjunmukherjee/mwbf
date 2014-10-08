package com.MWBFServer.Datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Utils;

public class DataCache 
{
	// TODO : 
	// 1. Get other classes to use this cache.
	// 2. Remove static for the member variables
	
	private static DataCache singleInstance;
	
	
	private static final Map<String,Activities> m_activitiesHash = new HashMap<String,Activities>();
	public static final Map<String,User> m_usersHash = new HashMap<String,User>();
	private final static Map<User,List<Friends>> m_friendsHash = new HashMap<User,List<Friends>>();
	
	private static final Logger log = Logger.getLogger(DataCache.class);
	
	static
	{
		// Load all the users into the cache
		Utils.loadUsers(null, m_usersHash);
		
		// Load all the MWBF activities into the cache
		loadActivities();
		
		// Load all user's friends into the cache
		loadFriends();
	}
	
	/**
	 * Add a set of activities to the hash map.
	 * Run --> Run Activity Object
	 * @param mActivitieshash
	 */
	@SuppressWarnings("unchecked")
	public static void loadActivities() 
	{
		log.info("Loading MWBF ACTIVITIES into CACHE.");
		
		List<Activities> activitiesList =  (List<Activities>) DbConnection.queryGetActivityList();
		for (Activities activity : activitiesList)
			m_activitiesHash.put(activity.getActivityName(), activity);
	}
	
	/**
     * Load all of a users friends into the hash
     * User1 --> Friend1, Friend2..
     * @param _mUserfriendshash
     */
	@SuppressWarnings("unchecked")
	public static void loadFriends() 
	{
		log.info("Loading FRIENDS into CACHE.");
		
		// Iterate through each of the users and load up their friends
		for (User user : m_usersHash.values())
			m_friendsHash.put(user, (List<Friends>) DbConnection.queryGetFriendsList(user));
	}
	
	/**
	 * Singleton class, to cache the data in memory for quick access
	 */
	private DataCache(){ }
	
	/**
	 * Returns the single cache instance (@ThreadSafe DCL)
	 * @return
	 */
	public static DataCache getInstance()
	{
		if (singleInstance == null)
		{
			synchronized(DataCache.class)
			{
				if ( singleInstance == null )
					singleInstance = new DataCache();
			}
		}
		return singleInstance;
	}
	
	/**
	 * Returns a copy of the list of the users
	 * @return
	 */
	public List<User> getUsers()
	{
		return (new ArrayList<User>(m_usersHash.values()));
	}
	
	/**
	 * Returns a copy of the list of the user's friends
	 * @return
	 */
	public List<Friends> getFriends(User _user)
	{
		return (new ArrayList<Friends>(m_friendsHash.get(_user)));
	}
	
	/**
	 * Returns a copy of the list of the user's friends
	 * @return
	 */
	public void addFriend(User _user, Friends _friend)
	{
		m_friendsHash.get(_user).add(_friend);
	}
	
	/**
	 * Returns an activity
	 * @return
	 */
	public Activities getActivity(String _activityId)
	{
		Activities act = m_activitiesHash.get(_activityId);
		if (act != null)
			return new Activities(act);
		else
			return null;
	}

}
