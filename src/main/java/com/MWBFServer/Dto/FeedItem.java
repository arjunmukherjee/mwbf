package com.MWBFServer.Dto;

import java.util.Date;

/**
 * Created by vpasari on 9/28/14.
 */
public class FeedItem {

    private String firstName;
    private String lastName;
    private String userId;
    private String activityName;
    private Double activityValue;
    private String activityUnit;
    private double points;
    private Date activityDate;
    private String feedPrettyString;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityUnit() {
        return activityUnit;
    }

    public void setActivityUnit(String activityUnit) {
        this.activityUnit = activityUnit;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    public String getFeedPrettyString() {
        return feedPrettyString;
    }

    public void setFeedPrettyString(String feedPrettyString) {
        this.feedPrettyString = feedPrettyString;
    }

    public Double getActivityValue() {
        return activityValue;
    }

    public void setActivityValue(Double activityValue) {
        this.activityValue = activityValue;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
    
    @Override
    public String toString()
    {
    	return this.feedPrettyString;
    }
}
