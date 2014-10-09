package com.MWBFServer.Activity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name="MWBF_ACTIVITIES")
public class Activities implements Serializable
{
	private static final long serialVersionUID = -749006219075703015L;
	
	private long id;
	private String activityName;
	private String pastVerb;
	private String measurementUnit;
	private String measurementUnitShort;
	private double pointsPerUnit;
	private double wholeUnit;
	
	
	protected Activities(){}
	
	/**
	 * Copy constructor.
	 * @param _activity
	 */
	public Activities(Activities _activity) 
	{
		id = _activity.id;
		activityName = _activity.activityName;
		pastVerb = _activity.pastVerb;
		measurementUnit = _activity.measurementUnit;
		measurementUnitShort = _activity.measurementUnitShort;
		pointsPerUnit = _activity.pointsPerUnit;
		wholeUnit = _activity.wholeUnit;
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
	
	@Column (name="ACTIVITY_NAME")
	public String getActivityName() 
	{
		return activityName;
	}
	public void setActivityName(String activityName) 
	{
		this.activityName = activityName;
	}
	
	@Column (name="POINTS_PER_UNIT")
	public double getPointsPerUnit() 
	{
		return pointsPerUnit;
	}
	public void setPointsPerUnit(double pointsPerUnit) 
	{
		this.pointsPerUnit = pointsPerUnit;
	}
	
	@Column (name="WHOLE_UNIT")
	public double getWholeUnit() 
	{
		return wholeUnit;
	}
	public void setWholeUnit(double wholeUnit) 
	{
		this.wholeUnit = wholeUnit;
	}
	
	@Column (name="MEASUREMENT_UNIT")
	public String getMeasurementUnit() 
	{
		return measurementUnit;
	}
	public void setMeasurementUnit(String measurementUnit) 
	{
		this.measurementUnit = measurementUnit;
	}
	
	@Column (name="MEASUREMENT_UNIT_SHORT")
	public String getMeasurementUnitShort() 
	{
		return measurementUnitShort;
	}
	public void setMeasurementUnitShort(String measurementUnitShort) 
	{
		this.measurementUnitShort = measurementUnitShort;
	}
	
	@Column (name="PAST_VERB")
	public String getPastVerb() 
	{
		return pastVerb;
	}
	public void setPastVerb(String pastVerb) 
	{
		this.pastVerb = pastVerb;
	}

	
	@Override
	public String toString()
	{
		return "Id [" + id + "], Name[" + activityName + "], PointsPerUnit[" + pointsPerUnit + "]";
	}

}
