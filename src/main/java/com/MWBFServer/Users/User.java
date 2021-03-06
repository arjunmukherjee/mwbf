package com.MWBFServer.Users;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Dto.UserDto;
import com.MWBFServer.Utils.BasicUtils;
import com.MWBFServer.Utils.JsonConstants;
import com.MWBFServer.Utils.Utils;
import com.MWBFServer.Utils.Utils.TimeAggregateBy;

@Entity
@Table (name="USER_DETAILS")
public class User implements Serializable
{
	private static final long serialVersionUID = 8391768001302298769L;
	private static final Logger log = Logger.getLogger(User.class);
	
	private String id;
	private String email;
	private String userName;
	private String firstName;
	private String lastName;
	private String fbProfileId;
	private Date memberSince;
	
	
	protected User(){}
	
	/**
	 * 
	 * @param _email
	 * @param _userName
	 */
	public User(String _email, String _firstName, String _lastName, String _fbProfileId)
	{
		id = _email;
		email = _email;
		userName = _email;
		firstName = _firstName;
		lastName = _lastName;
		fbProfileId = _fbProfileId;
		memberSince = new Date();
	}
	
	/**
	 * Registers a Facebook user
	 * @param _email
	 */
	public User(String _email)
	{
		id = _email;
		email = _email;
		memberSince = new Date();
	}

	/**
	 * Copy constructor
	 * @param user
	 */
	public User(User _user) 
	{
		this.id = _user.id;
		this.email = _user.email;
		this.memberSince = _user.memberSince;
		this.fbProfileId = _user.fbProfileId;
		this.firstName = _user.firstName;
		this.lastName = _user.lastName;
		this.userName = _user.userName;
	}

	@Id
	@Column (name="ID")
	public String getId() 
	{
		return id;
	}
	public void setId(String _id) 
	{
		id = _id;
	}
	
	
	@Column (name="USER_NAME")
	public String getUserName() 
	{
		return userName;
	}
	public void setUserName(String _userName) 
	{
		userName = _userName;
	}
	
	
	@Column (name="EMAIL")
	public String getEmail() 
	{
		return email;
	}
	public void setEmail(String _email) 
	{
		email = _email;
	}
	
	@Column (name="FIRST_NAME")
	public String getFirstName() 
	{
		return firstName;
	}
	public void setFirstName(String m_firstName) 
	{
		this.firstName = m_firstName;
	}

	
	@Column (name="LAST_NAME")
	public String getLastName() 
	{
		return lastName;
	}
	public void setLastName(String _lastName) 
	{
		this.lastName = _lastName;
	}
	
	
	@Column (name="MEMBER_SINCE")
	public Date getMemberSince() 
	{
		return memberSince;
	}
	public void setMemberSince(Date _memberSince) 
	{
		this.memberSince = _memberSince;
	}

	@Column (name="FB_PROFILE_ID")
	public String getFbProfileId() 
	{
		return fbProfileId;
	}
	public void setFbProfileId(String fbProfileId) 
	{
		this.fbProfileId = fbProfileId;
	}
	
	@Override
	public int hashCode()
	{
	    return new HashCodeBuilder().append(email).toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if(obj == this) return true;  // test for reference equality
	    if(obj == null) return false; // test for null
	    
	    if( getClass() == obj.getClass() )
	    {
	        final User other = (User) obj;
	        return new EqualsBuilder()
	            .append(email, other.email)
	            .isEquals();
	    } 
	    else
	        return false;
	}
	
	@Override
	public String toString()
	{
		return "User : Name [" + getFirstName() + " " + getLastName() + "] ,UserId[" + getId() + "], Email[" + getEmail() + "], UserName[" + getUserName() + "]";
	}
	
	/**
	 * Add the user : persist in DB and save in cache.
	 * @param newUser
	 * @return
	 */
	public String addUser() 
	{
		User newUser = this;
		String returnStr;
		
		// If successful, add to the local cache
		if ( DbConnection.saveObj(newUser) )
		{
			returnStr = BasicUtils.constructReturnString(JsonConstants.SUCCESS_YES, "Welcome !");
			CacheManager.getCache().addUser(newUser);
		}
		else
		{
			log.warn("Unable to register user [" + newUser.getEmail() + "], please try again.");
			returnStr = BasicUtils.constructReturnString(JsonConstants.SUCCESS_NO, "Unable to register user, please try again.");
		}
		
		return returnStr;
	}
	
	
	/**
	 * For each user , get their individual info
	 * 1. Get the challenge stats
	 * 2. Get the Points stats
	 * @return UserDto object
	 */
	public UserDto userInfo() 
	{
		List<UserActivityByTime> allTimeHighList = Utils.getAllTimeHighs(this);
		
		List<Integer> challengeStatsList = Utils.getChallengesStatsForUser(this);
		
		Double currentWeekPoints = Utils.getUsersPointsForCurrentTimeInterval(this,TimeAggregateBy.week);
		Double currentMonthPoints = Utils.getUsersPointsForCurrentTimeInterval(this,TimeAggregateBy.month);
		Double currentYearPoints = Utils.getUsersPointsForCurrentTimeInterval(this,TimeAggregateBy.year);
		
		UserDto userDtoObj = null;
		if ( ( allTimeHighList != null ) && ( allTimeHighList.size() > 2 )  )
			userDtoObj = new UserDto(this,currentWeekPoints,currentMonthPoints,currentYearPoints,challengeStatsList.get(0),challengeStatsList.get(1),challengeStatsList.get(2),allTimeHighList.get(0),allTimeHighList.get(1),allTimeHighList.get(2),allTimeHighList.get(3));
		else
		{
			UserActivityByTime emptyUserActivity = new UserActivityByTime("--", 0.0);
			userDtoObj = new UserDto(this,currentWeekPoints,currentMonthPoints,currentYearPoints,challengeStatsList.get(0),challengeStatsList.get(1),challengeStatsList.get(2),emptyUserActivity,emptyUserActivity,emptyUserActivity,emptyUserActivity);
		}
		
		return userDtoObj;
	}
}
