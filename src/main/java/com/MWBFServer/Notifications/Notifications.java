package com.MWBFServer.Notifications;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Users.User;


/**
 * Notifications class to save the user notifications on the server side
 * 1. Prevents the user from getting notified multiple times.
 * 2. Don't know how to access the FaceBook api from the server, only from the client.
 * @author arjunmuk
 *
 */
@Entity
@Table (name="NOTIFICATIONS")
public class Notifications implements Serializable
{
	private static final long serialVersionUID = 1683514395955271549L;
	
	private long id;
	private User user;
	private String notificationMessage;
	
	protected Notifications(){}
	
	public Notifications(User _user, String _notificationMessage) 
	{
		this.user = _user;
		this.notificationMessage = _notificationMessage;
	}
	
	public Notifications(User _user, ClientNotification cn) 
	{
		this.notificationMessage = cn.notificationMessage;
		if (_user == null)
			this.user = DataCache.getInstance().getUserByFbId(cn.fbProfileId);
		else
			this.user = _user;
	}
	
	@Id @GeneratedValue
	@Column (name="ID")
	public long getId() 
	{
		return id;
	}
	public void setId(long id) 
	{
		this.id = id;
	}
	
	@ManyToOne
    @JoinColumn(name = "USER_ID")
	public User getUser() 
	{
		return user;
	}
	public void setUser(User user) 
	{
		this.user = user;
	}
	
	@Column (name="NOTIFICATION_MESSAGE")
	public String getNotificationMessage() 
	{
		return notificationMessage;
	}
	public void setNotificationMessage(String notificationMessage) 
	{
		this.notificationMessage = notificationMessage;
	}
	
	@Override
	public String toString()
	{
		return "User [" + this.user.getId() + "] has message [" + this.notificationMessage + "]";
	}
	
	@Override
	public int hashCode()
	{
	    return new HashCodeBuilder().append(user).append(notificationMessage).toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if(obj == this) return true;  // test for reference equality
	    if(obj == null) return false; // test for null
	    
	    if( getClass() == obj.getClass() )
	    {
	        final Notifications other = (Notifications) obj;
	        return new EqualsBuilder()
	            .append(user, other.user)
	            .append(notificationMessage, other.notificationMessage)
	            .isEquals();
	    } 
	    else
	        return false;
	}
	
	public class ClientNotification
	{
		public String userId;
		public String friendUserId;
		public String fbProfileId;
		public String notificationMessage;
		
		@Override
		public String toString()
		{
			return "User[" + userId + "], FB[" + fbProfileId + "], Message [" + notificationMessage + "]";
		}
	}
}
