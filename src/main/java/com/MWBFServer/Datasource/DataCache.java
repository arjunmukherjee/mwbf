package com.MWBFServer.Datasource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Notifications.Notifications;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Constants;

public class DataCache 
{
	private static DataCache m_cacheInstance;
	private static final Logger log = Logger.getLogger(DataCache.class);
	
	private static final Map<String,Activities> m_MWBFActivitiesHash = new HashMap<String,Activities>();
	private static final Map<String,User> m_usersHashByEmailId = new HashMap<String,User>();
	private static final Map<String,User> m_usersHashByFbId = new HashMap<String,User>();
	private static final Map<User,List<Friends>> m_friendsHash = new HashMap<User,List<Friends>>();
	private static final Map<User,List<UserActivity>> m_userActivitiesHash = new HashMap<User,List<UserActivity>>();
	private static final Map<User,List<Challenge>> m_userChallengesHash = new HashMap<User,List<Challenge>>();
	private static final Map<User,List<Notifications>> m_userNotifications = new HashMap<User,List<Notifications>>();
	
	/**
	 * Singleton class, to cache the data in memory for quick access
	 */
	private DataCache()
	{ 
		loadData();
	}
	
	/**
	 * Returns the single cache instance (@ThreadSafe DCL)
	 * @return
	 */
	public static DataCache getInstance()
	{
		if (m_cacheInstance == null)
		{
			synchronized(DataCache.class)
			{
				if ( m_cacheInstance == null )
					m_cacheInstance = new DataCache();
			}
		}
		return m_cacheInstance;
	}
	
	
	/**
	 * Load all the data into the cache
	 */
	private void loadData()
	{
		// Load all the users into the cache
		loadUsers();

		// Load all the MWBF activities into the cache
		loadMWBFActivities();

		// Load all user's friends into the cache
		loadFriends();
		
		// Load all the user's activities into the cache
		loadUserActivities();
		
		// Load all the user's challenges into the cache
		loadUserChallenges();
		
		// Load all the user's notifications into the cache
		loadUserNotifications();
	}
	
	/**
	 * Load all the users from the Database into the validUsers hashSet.
	 * User lookup becomes fast.
	 */
	@SuppressWarnings("unchecked")
	private void loadUsers()
	{
		log.info("Loading USERS into CACHE.");
		
		List<User> userList = (List<User>) DbConnection.queryGetUsers();
		for (User user : userList)
		{
			m_usersHashByEmailId.put(user.getEmail(),user);
			
			if ( ( user.getFbProfileId() != null ) && ( user.getFbProfileId().length() > 0 ) )
				m_usersHashByFbId.put(user.getFbProfileId(),user);
		}
	}
	
	/**
	 * Load all the user's activities from the Database into the hash.
	 */
	@SuppressWarnings("unchecked")
	private void loadUserActivities()
	{
		// TODO : Too much data here. Will soon become unmanageable
		// TODO : Optimization 1 : Load only current year
		log.info("Loading USER-ACTIVITIES into CACHE.");
		for (User user : m_usersHashByEmailId.values())
            m_userActivitiesHash.put(user, (List<UserActivity>) DbConnection.queryGetUserActivity(user));
    }
	
	/**
	 * Add a set of activities to the hash map.
	 * Run --> Run Activity Object
	 * @param mActivitieshash
	 */
	@SuppressWarnings("unchecked")
	private void loadMWBFActivities() 
	{
		log.info("Loading MWBF-ACTIVITIES into CACHE.");
		
		List<Activities> activitiesList =  (List<Activities>) DbConnection.queryGetActivityList();
		for (Activities activity : activitiesList)
			m_MWBFActivitiesHash.put(activity.getActivityName(), activity);
	}
	
	/**
     * Load all of a users friends into the hash
     * User1 --> Friend1, Friend2..
     * @param _mUserfriendshash
     */
	@SuppressWarnings("unchecked")
	private void loadFriends() 
	{
		log.info("Loading USER-FRIENDS into CACHE.");
		
		// Iterate through each of the users and load up their friends
		for (User user : m_usersHashByEmailId.values())
			m_friendsHash.put(user, (List<Friends>) DbConnection.queryGetFriendsList(user));
	}
	
	/**
	 * Load all the user's challenges from the Database into the hash.
	 * UserX --> Challenge1, Challenge2
	 */
	@SuppressWarnings("unchecked")
	private void loadUserChallenges()
	{
		log.info("Loading USER-CHALLENGES into CACHE.");
	
		List<Challenge> listCh = (List<Challenge>) DbConnection.queryGetChallengesHQL();
		for (Challenge ch : listCh)
			addChallengeToPlayers(ch);
	}
	
	/**
	 * Load all the user's notifications from the Database into the hash.
	 * UserX --> Notification1, Notification2
	 */
	@SuppressWarnings("unchecked")
	private void loadUserNotifications()
	{
		log.info("Loading USER-NOTIFICATIONS into CACHE.");
	
		List<Notifications> notList = (List<Notifications>) DbConnection.queryGetNotifications();
		
		// Iterate through each of the users and load up their notifications
		if ( ( notList != null ) && ( notList.size() > 0 ) )
		{
			for (Notifications not : notList)
			{
				List<Notifications> userNotList = m_userNotifications.get(not.getUser());
				if ( userNotList == null )
					userNotList = new ArrayList<Notifications>();
				
				userNotList.add(not);
				m_userNotifications.put(not.getUser(), userNotList);
			}
		}
	}
	
	/**
	 * Returns a copy of the list of the user's notifications
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Notifications> getUserNotifications(User _user)
	{
		List<Notifications> notificationList = m_userNotifications.get(_user);
		if ( ( notificationList != null ) && ( notificationList.size() > 0 ) )
			return (List<Notifications>) copyCollection(new ArrayList<Notifications>(notificationList));
		else
			return null;
	}
	
	public void addNotification(Notifications not) 
	{
		List<Notifications> userNotList = m_userNotifications.get(not.getUser());
		if ( userNotList == null )
			userNotList = new ArrayList<Notifications>();
		
		userNotList.add(not);
		m_userNotifications.put(not.getUser(), userNotList);
	}
	
	
	/**
	 * Returns a copy of the list of the users
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers()
	{
		return (List<User>) copyCollection(new ArrayList<User>(m_usersHashByEmailId.values()));
	}
	
	/**
	 * Returns an user
	 * @return
	 */
	public User getUserById(String _userId)
	{
		User user = m_usersHashByEmailId.get(_userId);
		if (user != null)
			return new User(user);
		else
			return null;
	}
	
	/**
	 * Returns an user, lookup by the Facebook profile id
	 * @return
	 */
	public User getUserByFbId(String _fbProfileId)
	{
		User user = m_usersHashByFbId.get(_fbProfileId);
		
		if (user != null)
			return new User(user);
		else
			return null;
	}
	
	/**
	 * Returns a list of users that have a first or last name that starts with the argument passed in
	 * @return List(User)
	 */
	public List<User> getUserByName(String _name)
	{
		List<User> returnList = null;
		StringBuilder fullName = new StringBuilder();;
		if ( ( _name != null ) && ( _name.length() > 0 ) )
		{
			returnList = new ArrayList<User>();
			_name = _name.toLowerCase();
			
			for (User user : m_usersHashByEmailId.values())
			{
				// Check the whole name "First Last"
				if ( _name.contains(" ") )
				{
					fullName.append(user.getFirstName().toLowerCase());
					fullName.append(" ");
					fullName.append(user.getLastName().toLowerCase());
					
					if (fullName.toString().equals(_name))
						returnList.add(user);
					
					fullName.delete(0, fullName.length());
				}
				else if ( user.getFirstName().toLowerCase().startsWith(_name) || user.getLastName().toLowerCase().startsWith(_name) )
					returnList.add(user);
				
				if ( returnList.size() == Constants.MAX_FRIENDS_SEARCH_RESULTS )
					return returnList;
			}
		}
		
		return returnList;
	}
	
	
	/**
	 * Adds a user to the cache
	 * @return
	 */
	public void addUser(User _user)
	{
		m_usersHashByEmailId.put(_user.getEmail(),_user);
	}
	
	/**
	 * Returns a copy of the list of the user's friends
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Friends> getFriends(User _user)
	{
		if ( ( m_friendsHash.get(_user) != null ) && ( m_friendsHash.get(_user).size() > 0 ) )
			return (List<Friends>) copyCollection(new ArrayList<Friends>(m_friendsHash.get(_user)));
		else 
			return null;
	}
	
	/**
	 * Adds a friend to the user's friends list
	 * @return
	 */
	public void addFriend(User _user, Friends _friend)
	{
		List<Friends> usersFriendsList = m_friendsHash.get(_user);
		
		// Add the friend to the users friend list
		if (usersFriendsList == null)
		{
			usersFriendsList = new ArrayList<Friends>();
			usersFriendsList.add(_friend);
			m_friendsHash.put(_user, usersFriendsList);
		}
		else
			m_friendsHash.get(_user).add(_friend);
		
		// Add the user to the friend's friendList
		User friendUser = m_usersHashByEmailId.get(_friend.getId());
		List<Friends> friendsFriendsList = m_friendsHash.get(friendUser);
		Friends friend = new Friends(friendUser,_user);
		if (friendsFriendsList == null)
		{
			friendsFriendsList = new ArrayList<Friends>();
			friendsFriendsList.add(friend);
			m_friendsHash.put(friendUser, friendsFriendsList);
		}
		else
			m_friendsHash.get(friendUser).add(friend);
	}
	
	/**
	 * Returns an activity
	 * @return
	 */
	public Activities getMWBFActivity(String _activityId)
	{
		Activities act = m_MWBFActivitiesHash.get(_activityId);
		if (act != null)
			return new Activities(act);
		else
			return null;
	}
	
	/**
	 * Returns a copy of the list of all valid activities
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Activities> getMWBFActivities()
	{
		return (List<Activities>) copyCollection(new ArrayList<Activities>(m_MWBFActivitiesHash.values()));
	}
	
	/**
	 * Returns a copy of the list of the user's activities
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserActivity> getUserActivities(User _user)
	{
		List<UserActivity> userActivityList = m_userActivitiesHash.get(_user);
		if ( ( userActivityList != null ) && ( userActivityList.size() > 0 ) )
			return (List<UserActivity>) copyCollection(new ArrayList<UserActivity>(userActivityList));
		else
			return null;
	}
	
	/**
	 * List of all user activities between a specific date range.
	 * 
	 * @param _user
	 * @param _fromDate
	 * @param _toDate
	 * @return List(UserActivity)
	 */
	@SuppressWarnings("unchecked")
	public List<UserActivity> getUserActivitiesFilterByDate(User _user, Date _fromDate, Date _toDate)
	{
		// TODO : Implement this
		return (List<UserActivity>) copyCollection(new ArrayList<UserActivity>(m_userActivitiesHash.get(_user)));
	}
	
	/**
	 * List of specific user activities between a specific date range.
	 * 
	 * @param _user
	 * @param _fromDate
	 * @param _toDate
	 * @param _activity
	 * @return List(UserActivity)
	 */
	@SuppressWarnings("unchecked")
	public List<UserActivity> getUserActivitiesFilterByDateAndActivity(User _user, Date _fromDate, Date _toDate, String _activity)
	{
		// TODO : Implement this
		return (List<UserActivity>) copyCollection(new ArrayList<UserActivity>(m_userActivitiesHash.get(_user)));
	}
	
	/**
	 * Adds a logged activity to the user's activity list
	 * @param _ua (UserActivity)
	 */
	public void addUserActivity(UserActivity _ua)
	{
		User user = getUserById(_ua.getUser().getId());
		_ua.setUser(user);
		if (user == null)
			log.warn("Unable to find user [" + _ua.toString() + "] to cache activity.");
		else
		{
			List<UserActivity> userActivityList = m_userActivitiesHash.get(user);
			if ( userActivityList == null) 
			{
				userActivityList = new ArrayList<UserActivity>();
				userActivityList.add(_ua);
				m_userActivitiesHash.put(user, userActivityList);
			}
			else
				m_userActivitiesHash.get(user).add(_ua);
		}
	}
	
	/**
	 * Returns a copy of the list of the user's challenges
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Challenge> getUserChallenges(User _user)
	{
		List<Challenge> challengeList = m_userChallengesHash.get(_user);
		if ( ( challengeList != null ) && ( challengeList.size() > 0 ) )
			return (List<Challenge>) copyCollection(new ArrayList<Challenge>(challengeList));
		else
			return null;
	}
	
	/**
	 * Add a challenge to the cache
	 * @param _ch (Challenge)
	 */
	public void addChallenge(Challenge _ch)
	{
		addChallengeToPlayers(_ch);
	}
	
	/**
	 * For each player in the challenge add the challenge to their list of challenges (if it does not already exist)
	 * @param _ch
	 */
	private void addChallengeToPlayers(Challenge _ch)
	{
		for (String userId : _ch.getPlayersSet())
		{
			User user = getUserById(userId);
			if ( m_userChallengesHash.containsKey(user) )
			{
				boolean challengeAdded = false;
				List<Challenge> challengeList = m_userChallengesHash.get(user);
				for (Challenge challenge : challengeList)
				{
					if (challenge.getId() == _ch.getId())
						challengeAdded = true;
				}
				
				if ( !challengeAdded )
					m_userChallengesHash.get(user).add(_ch);
			}
			else
			{
				List<Challenge> challengeList = new ArrayList<Challenge>();
				challengeList.add(_ch);
				m_userChallengesHash.put(user, challengeList);
			}
		}
	}
	
	
	/**
	 * Return a deep copy of the arrayList (using JOS)
	 * @param _collectionToCopy
	 * @return
	 */
	public List<?> copyCollection(List<?> _collectionToCopy)
	{
		Object obj = null;
        try 
        {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(_collectionToCopy);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) 
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) 
        {
            cnfe.printStackTrace();
        }
        return (List<?>) obj;
	}

}
