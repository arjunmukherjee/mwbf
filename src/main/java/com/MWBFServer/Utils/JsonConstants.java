package com.MWBFServer.Utils;

/**
 * Final Abstract class to hold the string literals to help parse the Json objects.
 * @author arjunmuk
 *
 */
public final class JsonConstants 
{
	/**
	 * Private constructor. This class is not to be instantiated.
	 */
	private JsonConstants()
	{
        throw new IllegalStateException( "Do not instantiate this class." );
    }
	
	// The strings used to identify user's personal info (email is the key)
	public static final String EMAIL = "email";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String PROFILE_ID = "profileId";
	
	// The strings used to lookup an user (email is the key)
	public static final String USER_ID = "user_id";
	public static final String USERID = "userId";
	
	// The strings used to lookup a user. Can be the first name, last name, email, partial name etc
	public static final String USER_IDENTIFICATION_STRING = "userIdentification";
	
	// The strings used to identify friend related fields (email is the key)
	public static final String FRIEND_USER_ID = "friend_user_id";	
	public static final String FRIEND_REQUEST_ID = "friend_request_id";
	public static final String FRIEND_REQUEST_ACTION = "friend_request_action";
	
	public static final String CHALLENGE_ID = "challenge_id";
	
	// The strings used to pull up activity details
	public static final String FROM_DATE = "from_date";
	public static final String TO_DATE = "to_date";
	public static final String ACTIVITY_ID = "activityId";
	
	// The strings used to indicate success or failure
	public static final String SUCCESS_YES = "1";
	public static final String SUCCESS_NO = "0";
}
