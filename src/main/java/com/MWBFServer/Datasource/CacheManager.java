package com.MWBFServer.Datasource;

import java.util.Date;
import java.util.List;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Challenges.Challenge;
import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Notifications.Notifications;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;

public abstract class CacheManager 
{
	// Returns a copy of the list of the user's notifications
	public abstract List<Notifications> getUserNotifications(User _user);
	public abstract void addNotification(Notifications not);
	
	// Returns a copy of the list of the users
	public abstract List<User> getUsers();
	// Returns an user
	public abstract User getUserById(String _userId);
	// Returns an user, lookup by the Facebook profile id
	public abstract User getUserByFbId(String _fbProfileId);
	// Returns a list of users that have a first or last name that starts with the argument passed in
	public abstract List<User> getUserByName(String _name);
	// Adds a user to the cache
	public abstract void addUser(User _user);
	
	// Returns a copy of the list of the user's friends
	public abstract List<User> getFriends(User _user);
	// Adds a friend to the user's friends list
	public abstract void addFriend(User _user, Friends _friend);
	
	// Returns an activity
	public abstract Activities getMWBFActivity(String _activityId);
	// Returns a copy of the list of all valid activities
	public abstract List<Activities> getMWBFActivities();
	// Returns a copy of the list of the user's activities
	public abstract List<UserActivity> getUserActivities(User _user);
	// List of all user activities between a specific date range.
	public abstract List<UserActivity> getUserActivitiesFilterByDate(User _user, Date _fromDate, Date _toDate);
	// List of specific user activities between a specific date range.
	public abstract List<UserActivity> getUserActivitiesFilterByDateAndActivity(User _user, Date _fromDate, Date _toDate, String _activity);
	// Adds a logged activity to the user's activity list
	public abstract void addUserActivity(UserActivity _ua);
	// delete an activity from the user's activity list
	public abstract void deleteUserActivity(UserActivity _ua);
	// Returns a list of feed items from the users feeds.
	public abstract List<FeedItem> getActivityFeed(User _user);
	
	// Returns a copy of the list of the user's challenges
	public abstract List<Challenge> getUserChallenges(User _user);
	// For each player in the challenge add the challenge to their list of challenges (if it does not already exist)
	public abstract void addChallenge(Challenge _ch);
	// For each player in the challenge update the challenge to their list of challenges 
	public abstract void updateChallenge(Challenge _ch);
	
	/**
	 * Returns an instance of the cache implementation.
	 * Currently it is the simple HashMap based caching.
	 * @return
	 */
	public static CacheManager getCache()
	{
		return SimpleCache.getInstance();
	}
}
