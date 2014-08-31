package com.MWBFServer.Datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Utils;

public class DataCache 
{
	private static DataCache singleInstance;
	
	public static final Map<String,Activities> m_activitiesHash = new HashMap<String,Activities>();
	public static final Map<String,User> m_usersHash = new HashMap<String,User>();
	
	static
	{
		// Load all the users into the cache
		Utils.loadUsers(null, m_usersHash);
		
		// Load all the MWBF activities into the cache
		Utils.loadActivities(m_activitiesHash);
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
