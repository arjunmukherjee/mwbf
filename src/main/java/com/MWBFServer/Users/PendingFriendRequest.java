package com.MWBFServer.Users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Utils.BasicUtils;

@Entity
@Table (name="PENDING_FRIEND_REQUEST")
public class PendingFriendRequest implements Serializable
{
	// TODO : Don't know why, but hibernate insists on having setter methods
	// thus unable to mark member variable as final

	private static final long serialVersionUID = -7956200177347991322L;
	private static final Logger log = Logger.getLogger(PendingFriendRequest.class);
	
	private long id;
	private String userId;
	private String friendId;
	
	protected PendingFriendRequest(){}
	
	/**
	 * 
	 * @param _userId
	 * @param _friendId
	 */
	public PendingFriendRequest(String _userId, String _friendId)
	{
		userId = _userId;
		friendId = _friendId;
	}
	
	@Id @GeneratedValue
	@Column (name="ID")
	public long getId() 
	{
		return id;
	}
	public void setId(long _id) 
	{
		this.id = _id;
	}
	
	@Column (name="USER_ID")
	public String getUserId() 
	{
		return userId;
	}
	public void setUserId(String _userId) 
	{
		this.userId = _userId;
	}
	
	@Column (name="FRIEND_ID")
	public String getFriendId() 
	{
		return friendId;
	}
	public void setFriendId(String _friendId) 
	{
		this.friendId = _friendId;
	}
	
	@Override
	public int hashCode()
	{
	    return new HashCodeBuilder().append(userId).append(friendId).toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if(obj == this) return true;  // test for reference equality
	    if(obj == null) return false; // test for null
	    
	    if( getClass() == obj.getClass() )
	    {
	        final PendingFriendRequest other = (PendingFriendRequest) obj;
	        
	        if ( this.userId.equals(other.userId) && this.friendId.equals(other.friendId) )
	        	return true;
	        
	        if ( this.userId.equals(other.friendId) && this.friendId.equals(other.userId) )
	        	return true;
	        
	        return false;
	    } 
	    else
	        return false;
	}
	
	@Override
	public String toString()
	{
		return "User : [" + userId + "] , Friend[" + friendId + "]";
	}
	
	
	/**
	 * Returns a request item by the requestId.
	 * @param _requestId
	 * @return
	 */
	private static PendingFriendRequest getFriendRequest(String _requestId)
	{
		PendingFriendRequest pendFriendReqRet = null;
		
		@SuppressWarnings("unchecked")
		List<PendingFriendRequest> friendRequestList = (List<PendingFriendRequest>) DbConnection.queryGetFriendRequests(_requestId,null);
		if ( ( friendRequestList != null ) && ( friendRequestList.size() > 0 ) )
			pendFriendReqRet = friendRequestList.get(0);
		
		return pendFriendReqRet;
	}
	
	/**
	 * Reject a request, just delete the pending request.
	 * @param _requestId
	 * @return
	 */
	public static boolean rejectFriendRequest(String _requestId) 
	{
		// TODO : Must find a way to notify the requester that the "friend" has rejected the request
		PendingFriendRequest friendRequest = getFriendRequest(_requestId);
		
		boolean success = true;
		if ( friendRequest != null )
			DbConnection.deleteObject(friendRequest);
		else
			success = false;
		
		return success;
	}
	
	
	/**
	 * First find the friend request object.
	 * Extract the user and the friend from the object.
	 * Insert the rows into the friends table and add to the cache.
	 * Delete the friendAcceptRequest.
	 * @param _friendRequestId
	 * @return success [TRUE|FALSE]
	 */
	public static boolean acceptFriendRequest(String _requestId) 
	{
		PendingFriendRequest friendRequest = getFriendRequest(_requestId);
		boolean success = true;
		if ( friendRequest != null )
		{
			CacheManager cache = BasicUtils.getCache();
			User user = cache.getUserById(friendRequest.getUserId());
			User friend = cache.getUserById(friendRequest.getFriendId());
			
			// First check if the user and the friend are already friends
			if ( ( cache.getFriends(user) != null ) && cache.getFriends(user).contains(friend) )
			{
				log.warn("Duplicate friend request.. User [" + user.getEmail() + "] and friend [" + friend.getEmail() + "] are already friends.");
				return true;
			}
			
			Friends friendObj = new Friends(user,friend);
			Friends userObj = new Friends(friend,user);
			
			List<Friends> friendsList = new ArrayList<Friends>();
			friendsList.add(userObj);
			friendsList.add(friendObj);
			
			// Add the friend to the db and cache
			// Remove the pending request 
			success = DbConnection.saveList(friendsList);
			if ( success )
			{
				cache.addFriend(user, friendObj);
				DbConnection.deleteObject(friendRequest);
				
				log.info("[" + user.getFirstName() + "] and [" + friend.getFirstName() + "] are now friends.");
			}
		}
		else
			success = false;
		
		return success;
	}
	
	/**
	 * Fetches a list of all the pending friend requests for a given user.
	 * @param _user
	 * @return List<PendingFriendRequest>
	 */
	@SuppressWarnings("unchecked")
	public static List<PendingFriendRequest> getFriendRequests(User _user) 
	{
		return (List<PendingFriendRequest>) DbConnection.queryGetFriendRequests(null,_user.getId());
	}
	
	/**
	 * Add a friend to a user.
	 * @param _user
	 * @param _friend
	 * @return
	 */
	public static boolean addFriendRequest(User _user, User _friend) 
	{
		boolean result = false;
		PendingFriendRequest friendReq = new PendingFriendRequest(_user.getId(),_friend.getId());

		// Check if it is a duplicate friend request
		@SuppressWarnings("unchecked")
		List<PendingFriendRequest> friendReqList = (List<PendingFriendRequest>) DbConnection.queryGetFriendRequests(null,_user.getId());
		if ( (friendReqList != null) && (friendReqList.size() > 0 ) && friendReqList.contains(friendReq) )
			log.info("Duplicate friend request , not adding User[" + _user.getEmail() + "], Friend [" + _friend.getEmail() + "]");
		else
			result = DbConnection.saveObj(friendReq);
		
		return result;
	}	
}
