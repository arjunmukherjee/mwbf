package com.MWBFServer.Dto;

import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Users.User;

@SuppressWarnings("unused")
public class FriendsDto 
{
	private User user;
	private Double currentWeekPoints;
	private Double currentMonthPoints;
	private Double currentYearPoints;
	private int numberOfTotalChallenges;
	private int numberOfActiveChallenges;
	private int numberOfWonChallenges;
	private UserActivityByTime bestDay;
	private UserActivityByTime bestWeek;
	private UserActivityByTime bestMonth;
	private UserActivityByTime bestYear;
	
	public FriendsDto(User _user, Double _currentWeekPoints,Double _currentMonthPoints,Double _currentYearPoints,int _numberOfTotalChallenges, int _numberOfActiveChallenges,int _numberOfWonChallenges, UserActivityByTime _bestDay, UserActivityByTime _bestWeek, UserActivityByTime _bestMonth, UserActivityByTime _bestYear)
	{
		user = _user;
		currentWeekPoints = _currentWeekPoints;
		currentMonthPoints = _currentMonthPoints;
		currentYearPoints = _currentYearPoints;
		numberOfTotalChallenges = _numberOfTotalChallenges;
		numberOfActiveChallenges = _numberOfActiveChallenges;
		numberOfWonChallenges = _numberOfWonChallenges;
		bestDay = _bestDay;
		bestWeek = _bestWeek;
		bestMonth = _bestMonth;
		bestYear = _bestYear;
	}
}
