package com.MWBFServer.Utils;

public final class Constants 
{
	/**
	 * Private constructor. This class is not to be instantiated.
	 */
	private Constants()
	{
        throw new IllegalStateException( "Do not instantiate this class." );
    }
	
	// The maximum number of messages to display in a users feed
	public static int MAX_NUMBER_OF_MESSAGE_FEEDS = 100;
	
	// The minimum number of exercises per week the user must do in order to get the cross training bonus
	public static int NUMBER_OF_EXERCISES_FOR_CROSS_TRAINING_BONUS = 4;
	
	// The minimum number of points per exercise per week the user must earn in order to get the cross training bonus
	public static int POINTS_PER_EXERCISE_FOR_CROSS_TRAINING_BONUS = 8;
	
	// The string used to identify a bonus activity
	public static String BONUS_ACTIVITY_IDENTIFIER = "Bonus";
	
	// The string used to identify an accept action on a request
	public static String REQUEST_ACCEPT = "Accept";
	
	// The maximum number of search results returned for a free text friend search
	public static int MAX_FRIENDS_SEARCH_RESULTS = 5;
	
	// The hour of the day when the bonus service will run
	public static int HOUR_OF_DAY_TO_RUN_BONUS_CHECK = 23;
	
	// The hour of the day when the bonus service will run
	public static String CACHE_UPDATER_THREAD_NAME = "CacheUpdater";
}
