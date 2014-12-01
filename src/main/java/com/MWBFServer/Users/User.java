package com.MWBFServer.Users;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table (name="USER_DETAILS")
public class User implements Serializable
{
	private static final long serialVersionUID = 8391768001302298769L;
	
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
}
