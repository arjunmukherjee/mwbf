package com.MWBFServer.Datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static final Map<String,Activities> m_activitiesHash = new HashMap<String,Activities>();
	public static final Map<String,User> m_usersHash = new HashMap<String,User>();
	public static final Map<User,List<Friends>> m_friendsHash = new HashMap<User,List<Friends>>();
	
	static
	{
		// Load all the users into the cache
		Utils.loadUsers(null, m_usersHash);
		
		// Load all the MWBF activities into the cache
		Utils.loadActivities(m_activitiesHash);
		
		// Load all user's friends into the cache
		Utils.loadFriends(m_friendsHash);
	}
	
	/**
	 * Singleton class, to cache the data in memory for quick access
	 */
	private DataCache(){ }
	
	/**
	 * Returns the single cache instance (@NotThreadSafe)
	 * @return
	 */
	public static DataCache getInstance()
	{
		if (singleInstance == null)
			singleInstance = new DataCache();
		
		return singleInstance;
	}

}
