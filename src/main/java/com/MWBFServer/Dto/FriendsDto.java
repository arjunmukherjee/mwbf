package com.MWBFServer.Dto;

import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Users.User;

@SuppressWarnings("unused")
public class FriendsDto 
{
	private User user;
	private String currentWeekPoints;
	private int activeNumberOfChallanges;
	private UserActivityByTime bestDay;
	private UserActivityByTime bestWeek;
	private UserActivityByTime bestMonth;
	private UserActivityByTime bestYear;
	
	public FriendsDto(User _user, String _currentWeekPoints, int _activeNumberOfChallanges, UserActivityByTime _bestDay, UserActivityByTime _bestWeek, UserActivityByTime _bestMonth, UserActivityByTime _bestYear)
	{
		user = _user;
		currentWeekPoints = _currentWeekPoints;
		activeNumberOfChallanges = _activeNumberOfChallanges;
		bestDay = _bestDay;
		bestWeek = _bestWeek;
		bestMonth = _bestMonth;
		bestYear = _bestYear;
	}
}
