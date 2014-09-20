package com.MWBFServer.Activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Users.User;

@Entity
@Table (name="USER_ACTIVITY")
public class UserActivity implements Comparable<UserActivity>
{
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
	public UserActivity(User _user, String _activity, String _date, String _exerciseUnits)
	{
		user = _user;
		activityId = _activity;
		date = new Date();		// TODO
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
	public void setDate(Date m_date) 
	{
		this.date = m_date;
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
	 * Construct a notification string from an activity object.
	 * Ex : Radha ran 5mi on Sep 18
	 * @return
	 */
	public String constructNotificationString()
	{
 		StringBuilder actString = new StringBuilder();
 		String exerciseUnitsStr = Double.toString(this.exerciseUnits);
 		
 		// If the exercise amount is 5.0, then change it to 5
 		// If it is 5.6, leave it as 5.6
 		int exerciseUnitsInt = (int) this.exerciseUnits;
 		if (this.exerciseUnits == exerciseUnitsInt)
 			exerciseUnitsStr = Integer.toString(exerciseUnitsInt);
 		
 		actString.append(this.user.getFirstName());
 		actString.append(" ");
 		actString.append(DataCache.m_activitiesHash.get(this.activityId).getPastVerb());
 		actString.append(" ");
 		actString.append(exerciseUnitsStr);
 		actString.append(DataCache.m_activitiesHash.get(this.activityId).getMeasurementUnitShort());
 		actString.append(" on ");
 		actString.append(new SimpleDateFormat("MMM d").format(this.date));
 		
 		return actString.toString();
	}
	
	@Override
	public String toString()
	{
		return "User : [" + user.getId() + "], Activity [" + activityId + "], Units [" + exerciseUnits + "], Date [" + date.toString() + "]";
	}

	@Override
	public int compareTo(UserActivity o) 
	{
		return this.getDate().compareTo(o.getDate());
	}


	
}
