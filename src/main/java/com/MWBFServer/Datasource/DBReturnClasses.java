package com.MWBFServer.Datasource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Utils.TimeAggregateBy;


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
		
		public String getDate()
		{
			return date;
		}
		
		public String toString()
		{
			return "Points["+points+"], Date["+date+"]";
		}
	}
	
	public static class LeaderActivityByTime
	{
		private String date;
		private TimeAggregateBy aggUnit;
		private double points;
		private User user;
		
		/**
		 * 
		 * @param _user
		 * @param _date
		 * @param _points
		 * @param _aggUnit
		 */
		public LeaderActivityByTime(User _user,String _date, Double _points, TimeAggregateBy _aggUnit )
		{
			user = _user;
			points = _points;
			date = _date;
			aggUnit = _aggUnit;
		}
		
		public double getPoints()
		{
			return points;
		}
		
		public String toString()
		{
			return "User[" + user.getId() + "],Points["+points+"], Date["+date+"], AggregatedBy [" + aggUnit.name() + "]";
		}
	}
	
	public static class DBReturnChallenge 
	{
		@SuppressWarnings("unused")
		private long id;
		private String name;
		private String creatorId;
		private Date startDate;
		private Date endDate;
		private Set<String> playerPointsSet = new HashSet<String>();
		private Set<String> activitySet = new HashSet<String>();
		private List<String> messageList = new ArrayList<String>();
		
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
		
		public void setMessagesList(List<String> _messageList) 
		{
			messageList = _messageList;
		}
		
		public void setCreatorId(String _creatorId) 
		{
			this.creatorId = _creatorId;
		}
		
		@Override
		public String toString()
		{
			return "Name["+name+"], Creator[" + creatorId + "], StartDate["+startDate.toString()+"], EndDate["+endDate.toString()+"], NumberOfPlayers["+playerPointsSet.size()+"], NumberOfActivities["+activitySet.size()+"], NumberOfMessages[" + messageList.size() + "]";
		}	
	}

}
