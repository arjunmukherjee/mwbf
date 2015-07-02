package com.MWBFServer.Activity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Dto.FeedItem;
import com.MWBFServer.Services.CacheUpdaterContextListener;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.BasicUtils;
import com.MWBFServer.Utils.Constants;

@Entity
@Table (name="USER_ACTIVITY")
public class UserActivity implements Comparable<UserActivity>, Serializable
{
	private static final long serialVersionUID = 8072074771603090386L;
	private static final Logger log = Logger.getLogger(UserActivity.class);

	private long id;
	private User user;
	private String activityId;
	private Date date;
	private double exerciseUnits;
	private double points;
	
	protected UserActivity(){}
	
	/**
	 * 
	 * @param _user
	 * @param _activity
	 * @param _date
	 * @param _activityValue
	 */
	public UserActivity(User _user, String _activity, Date _date, String _exerciseUnits)
	{
		user = _user;
		activityId = _activity;
		date = _date;		
		exerciseUnits = Double.parseDouble(_exerciseUnits);
		points = 0;
	}
	
	/**
	 * 
	 * @param _user
	 * @param _activity
	 * @param _date
	 * @param _activityValue
	 */
	public UserActivity(User _user, String _activity, String _exerciseUnits)
	{
		user = _user;
		activityId = _activity;
		date = new Date();		
		exerciseUnits = Double.parseDouble(_exerciseUnits);
		points = 0;
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
	
	// TODO : Link to activity table with this indicator
	@Column (name="ACTIVITY_ID")
	public String getActivityId() 
	{
		return activityId;
	}
	public void setActivityId(String _activityId) 
	{
		this.activityId = _activityId;
	}
	
	@Column (name="ACTIVITY_DATE")
	public Date getDate() 
	{
		return date;
	}
	public void setDate(Date _date) 
	{
		this.date = _date;
	}
	
	@Column (name="EXERCISE_UNITS")
	public double getExerciseUnits() 
	{
		return exerciseUnits;
	}
	public void setExerciseUnits(double _exerciseUnits) 
	{
		this.exerciseUnits = _exerciseUnits;
	}
	
	@Column (name="POINTS")
	public double getPoints() 
	{
		return points;
	}
	public void setPoints(double _points) 
	{
		this.points = _points;
	}
	
	/**
	 * Convert the UserActivity into a FeedItem object.
	 * @return
	 */
	public FeedItem convertToFeedItem()
	{
		// Populate FeedItem object
		FeedItem feedItem = new FeedItem();
		feedItem.setId(this.getId());
		feedItem.setActivityDate(this.getDate());
		feedItem.setActivityName(this.getActivityId());

		if ( !this.isBonusActivity() )
			feedItem.setActivityUnit(BasicUtils.getCache().getMWBFActivity(this.getActivityId()).getMeasurementUnitShort());

		feedItem.setActivityValue(this.getExerciseUnits());
		feedItem.setFirstName(this.getUser().getFirstName());
		feedItem.setLastName(this.getUser().getLastName());
		feedItem.setUserId(this.getUser().getId());
		feedItem.setPoints(this.getPoints());
		feedItem.setFeedPrettyString(this.constructNotificationString());

		return feedItem;
	}

	/**
	 * Construct a notification string from an activity object.
	 * Ex : Radha ran 5mi on Sep 18
	 * @return
	 */
	public String constructNotificationString()
	{
 		StringBuilder actString = new StringBuilder();
 		actString.append(this.user.getFirstName());
 		actString.append(" ");
 		
 		CacheManager cache = BasicUtils.getCache();
 		
 		// Bonus activities do not have the info below
 		if ( !isBonusActivity() )
 		{
	 		String exerciseUnitsStr = Double.toString(this.exerciseUnits);
	 		
	 		// If the exercise amount is 5.0, then change it to 5
	 		// If it is 5.6, leave it as 5.6
	 		int exerciseUnitsInt = (int) this.exerciseUnits;
	 		if (this.exerciseUnits == exerciseUnitsInt)
	 			exerciseUnitsStr = Integer.toString(exerciseUnitsInt);
	 		
	 		
	 		actString.append(cache.getMWBFActivity(this.activityId).getPastVerb());
	 		actString.append(" ");
	 		actString.append(exerciseUnitsStr);
	 		actString.append(cache.getMWBFActivity(this.activityId).getMeasurementUnitShort());
	 		actString.append(" on ");
	 		actString.append(new SimpleDateFormat("MMM d").format(this.date));
 		}
 		else
 		{
 			actString.append("earned a ");
 			actString.append(this.activityId);
 			actString.append(" this week.");
 		}
 		
 		return actString.toString();
	}
	
	/**
	 * Check if an activity is a Bonus activity or a regular activity
	 * @return
	 */
	@Transient
	public boolean isBonusActivity()
	{
		if ( this.activityId.contains( Constants.BONUS_ACTIVITY_IDENTIFIER ) )
			return true;
		else
			return false;
	}
	
	@Override
	public String toString()
	{
		return "User[" + user.getId() + "]:[" + activityId + "]:Units[" + exerciseUnits + "]:Points[" + points + "]:Date[" + date.toString() + "]";
	}

	@Override
	public int compareTo(UserActivity _o) 
	{
		return _o.getDate().compareTo(this.getDate());
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(id).toHashCode();
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		if(obj == this) return true;  // test for reference equality
	    if(obj == null) return false; // test for null
	    
	    if( getClass() == obj.getClass() )
	    {
	        final UserActivity other = (UserActivity) obj;
	        return new EqualsBuilder()
	            .append(id, other.id)
	            .isEquals();
	    } 
	    else
	        return false;
	}
	
	
	/**
	 * Log the users activities.
	 * @param _userActivityList
	 * @return
	 */
	public static Boolean logActivity(List<UserActivity> _userActivityList)
	{
		// Multiply the activity's points * the number of exercise units and then store in db
		for (UserActivity ua : _userActivityList)
		{
			Activities act = BasicUtils.getCache().getMWBFActivity(ua.getActivityId());
			// Will get used while persisting bonus activities
			if ( act != null )
			{
				Double points = act.getPointsPerUnit() * ua.getExerciseUnits();
				points = BasicUtils.round(points, 1);
				ua.setPoints(points);
			}
		}

		// Save activity to DB
		boolean result = DbConnection.saveList(_userActivityList);
		
		// If DB save was successful, then save to local cache
		if ( result )
		{
			for (UserActivity ua : _userActivityList)
				CacheUpdaterContextListener.addTask(ua);
		}
		
		return result;
	}
	
	
	/**
	 * First find the activity object.
	 * Delete the activity object.
	 * @param _activityId
	 * @return
	 */
	public static boolean deleteActivity(String _activityId) 
	{
		boolean success = true;
	
		@SuppressWarnings("unchecked")
		List<UserActivity> activityList = (List<UserActivity>) DbConnection.queryGetActivity(_activityId);
		if ( ( activityList != null ) && ( activityList.size() > 0 ) )
		{
			UserActivity ua = activityList.get(0);
			
			if (ua != null )
				success = DbConnection.deleteActivity(ua);
			else
				success = false;
			
			// Delete the activity from the users cache
			if (success)
				BasicUtils.getCache().deleteUserActivity(ua);
		}
		else
		{
			log.warn("Could not find the activity with Id [" + _activityId + "]");
			success = false;
		}
		
		return success;
	}
}
