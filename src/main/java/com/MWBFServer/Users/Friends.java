package com.MWBFServer.Users;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table (name="USER_FRIENDS")
public class Friends implements Serializable
{
	private static final long serialVersionUID = -100900228898101351L;
	
	private long id;
	private User user;
	private User friend;
	
	protected Friends(){}
	
	/**
	 * 
	 * @param _user
	 * @param _friend
	 */
	public Friends(User _user, User _friend)
	{
		user = _user;
		friend = _friend;
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
	
	
	@ManyToOne
    @JoinColumn(name = "user_id")
	public User getUser() 
	{
		return user;
	}
	public void setUser(User _user) 
	{
		this.user = _user;
	}
	
	
	@ManyToOne
    @JoinColumn(name = "friend_user_id")
	public User getFriend() 
	{
		return friend;
	}
	public void setFriend(User _friend) 
	{
		this.friend = _friend;
	}
	

	@Override
	public String toString()
	{
		return "User : [" + user.getId() + "], FriendsList [" + friend.getId() + "]";
	}
}
