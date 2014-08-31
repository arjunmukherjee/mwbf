package com.MWBFServer.Datasource;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
	
	public static class DBReturnChallenge 
	{
		private long id;
		private String name;
		private String creatorId;
		private Date startDate;
		private Date endDate;
		private Set<String> playerPointsSet = new HashSet<String>();
		private Set<String> activitySet = new HashSet<String>();
		
		/**
		 * 
		 * @param _name
		 * @param _startDate
		 * @param _endDate
		 * @param _playersSet
		 * @param _activitySet
		 */
		public DBReturnChallenge(String _name, Date _startDate, Date _endDate, Set<String> _playerPointsSet, Set<String> _activitySet)
		{
			name = _name;
			startDate = _startDate;
			endDate = _endDate;
			playerPointsSet = _playerPointsSet;
			activitySet = _activitySet;
		}
		
		public void setId(long _id) 
		{
			id = _id;
		}
		
		public Date getStartDate() {
			return startDate;
		}
		
		public Date getEndDate() {
			return endDate;
		}
		
		public void setPlayersPointsSet(Set<String> _playerPointsSet) 
		{
			playerPointsSet = _playerPointsSet;
		}
		
		public void setActivitySet(Set<String> _activitySet) 
		{
			activitySet = _activitySet;
		}
		
		public void setCreatorId(String _creatorId) 
		{
			this.creatorId = _creatorId;
		}
		
		@Override
		public String toString()
		{
			return "Name["+name+"], Creator[" + creatorId + "], StartDate["+startDate.toString()+"], EndDate["+endDate.toString()+"], NumberOfPlayers["+playerPointsSet.size()+"], NumberOfActivities["+activitySet.size()+"]";
		}	
	}

}
