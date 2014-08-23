package com.MWBFServer.Datasource;

public class DBReturnClasses 
{
	public static class UserActivityByTime
	{
		private String date;
		private double points;
		
		public UserActivityByTime(String _date, Double _points )
		{
			points = _points;
			date = _date;
		}
		
		public double getPoints()
		{
			return points;
		}
		
		public String toString()
		{
			return "Points["+points+"], Date["+date+"]";
		}
	}

}
