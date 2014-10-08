package com.MWBFServer.Datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;

public class DataCache 
{
	private static DataCache singleInstance;
	private static final Logger log = Logger.getLogger(DataCache.class);
	
	private static final Map<String,Activities> m_activitiesHash = new HashMap<String,Activities>();
	private static final Map<String,User> m_usersHash = new HashMap<String,User>();
	private static final Map<User,List<Friends>> m_friendsHash = new HashMap<User,List<Friends>>();
	
	
	/**
	 * Load all the data into the cache
	 */
	public void loadData()
	{
		// Load all the users into the cache
		loadUsers();

		// Load all the MWBF activities into the cache
		loadActivities();

		// Load all user's friends into the cache
		loadFriends();
	}
	
	/**
	 * Load all the users from the Database into the validUsers hashSet.
	 * User lookup becomes fast.
	 */
	@SuppressWarnings("unchecked")
	public static void loadUsers()
	{
		log.info("Loading USERS into CACHE.");
		
		List<User> userList = (List<User>) DbConnection.queryGetUsers();
		for (User user : userList)
			m_usersHash.put(user.getEmail(),user);
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
	 * Returns an activity
	 * @return
	 */
	public User getUser(String _userId)
	{
		User user = m_usersHash.get(_userId);
		if (user != null)
			return new User(user);
		else
			return null;
	}
	
	/**
	 * Adds a user to the cache
	 * @return
	 */
	public void addUser(User _user)
	{
		m_usersHash.put(_user.getEmail(),_user);
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
	 * Adds a friend to the user's friends list
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
