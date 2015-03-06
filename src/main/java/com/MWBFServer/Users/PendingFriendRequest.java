package com.MWBFServer.Users;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table (name="PENDING_FRIEND_REQUEST")
public class PendingFriendRequest implements Serializable
{
	// TODO : Don't know why, but hibernate insists on having setter methods
	// thus unable to mark member variable as final

	private static final long serialVersionUID = -7956200177347991322L;
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
	
}
