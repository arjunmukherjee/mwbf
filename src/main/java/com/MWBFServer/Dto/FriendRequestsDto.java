package com.MWBFServer.Dto;

import com.MWBFServer.Users.User;

@SuppressWarnings("unused")
public class FriendRequestsDto 
{
	private final String userId;
	private final UserDto friend;
	private final long id;
	
	public FriendRequestsDto(String _userId, UserDto _friend, long _id)
	{
		userId = _userId;
		friend = _friend;
		id = _id;
	}

}
