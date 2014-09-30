package com.MWBFServer.Dto;

public class WeeklyComparisons 
{
	private double userPoints;
	private double friendsPointsAverage;
	private double leaderPoints;
	
	public WeeklyComparisons(double _userPoints, double _friendsPointsAverage, double _leaderPoints)
	{
		userPoints = _userPoints;
		friendsPointsAverage = _friendsPointsAverage;
		leaderPoints = _leaderPoints;
	}
	
	public double getFriendsPointsAverage() 
	{
		return friendsPointsAverage;
	}
	
	public double getUserPoints() 
	{
		return userPoints;
	}
	
	public double getLeaderPoints() 
	{
		return leaderPoints;
	}
	
}
