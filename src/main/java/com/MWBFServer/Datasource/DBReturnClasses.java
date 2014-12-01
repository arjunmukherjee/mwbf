package com.MWBFServer.Datasource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Utils.TimeAggregateBy;

@SuppressWarnings("unused")
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
	
	public static class PlayerActivityData 
	{
		private String userId;
		private Double totalPoints;
		private Map<String,Double> activityAggregateMap = new HashMap<String,Double>();
		
		public PlayerActivityData(String _userId, Double _totalPoints, Map<String, Double> _activityAggregateMap)
		{
			userId = _userId;
			totalPoints = _totalPoints;
			activityAggregateMap = _activityAggregateMap;
		}
		
		public Map<String,Double> getActivityAggregateMap()
		{
			return activityAggregateMap;
		}
		
		public void setActivityAggregateMap(Map<String, Double> _activityAggregateMap)
		{
			activityAggregateMap = _activityAggregateMap;
		}
		
		public String getUserId()
		{
			return userId;
		}
		
		public Double getTotalPoints()
		{
			return totalPoints;
		}
		public void setTotalPoints(Double _totalPoints)
		{
			totalPoints = _totalPoints;
		}
		
		@Override
		public String toString()
		{
			return "User [" + userId + "], TotalPoints [" + totalPoints + "], AggregateAct[" + activityAggregateMap.toString() + "]";
		}
	}
	
	public static class DBReturnChallenge 
	{
		private long id;
		private String name;
		private String creatorId;
		private Date startDate;
		private Date endDate;
		private Set<String> activitySet = new HashSet<String>();
		private List<String> messageList = new ArrayList<String>();
		private List<PlayerActivityData> playerActivityDataList = new ArrayList<PlayerActivityData>();
		
		/**
		 * 
		 * @param _name
		 * @param _startDate
		 * @param _endDate
		 * @param _playersSet
		 * @param _activitySet
		 */
		public DBReturnChallenge(String _name, Date _startDate, Date _endDate, Set<String> _activitySet)
		{
			name = _name;
			startDate = _startDate;
			endDate = _endDate;
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
		
		public void setPlayerActivityData(List<PlayerActivityData> _playerActivityDataList) 
		{
			playerActivityDataList = _playerActivityDataList;
		}
		
		@Override
		public String toString()
		{
			return "Name["+name+"], Creator[" + creatorId + "], StartDate["+startDate.toString()+"], EndDate["+endDate.toString()+"], NumberOfPlayers["+playerActivityDataList.size()+"], NumberOfActivities["+activitySet.size()+"], NumberOfMessages[" + messageList.size() + "]";
		}	
	}

}
