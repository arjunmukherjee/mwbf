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
	private String password;
	private String firstName;
	private String lastName;
	private Date memberSince;
	
	
	protected User(){}
	
	/**
	 * 
	 * @param _email
	 * @param _password
	 * @param _userName
	 */
	public User(String _email, String _password, String _firstName, String _lastName)
	{
		id = _email;
		email = _email;
		password = _password;
		userName = _email;
		firstName = _firstName;
		lastName = _lastName;
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
	
	
	@Column (name="PASSWORD")
	public String getPassword() 
	{
		return password;
	}
	public void setPassword(String m_password) 
	{
		this.password = m_password;
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

	
	@Override
	public int hashCode()
	{
	    return new HashCodeBuilder().append(id).append(userName).append(password).toHashCode();
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
	            .append(id, other.id)
	            .append(userName, other.userName)
	            .append(password, other.password)
	            .isEquals();
	    } 
	    else
	        return false;
	}
	
	@Override
	public String toString()
	{
		return "User : UserId[" + getId() + "], Email[" + getEmail() + "], UserName[" + getUserName() + "], Password[" + getPassword() + "]";
	}
}
