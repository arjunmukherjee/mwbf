package com.MWBFServer.Challenges;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.MWBFServer.Users.User;

@Entity
@Table (name="MWBF_CHALLENGES")
public class Challenge 
{
	private long id;
	private User creator;
	private String name;
	private Date startDate;
	private Date endDate;
	private Set<String> playersSet = new HashSet<String>();
	private Set<String> activitySet = new HashSet<String>();
	
	protected Challenge(){}
	
	/**
	 * 
	 * @param _name
	 * @param _startDate
	 * @param _endDate
	 * @param _playersSet
	 * @param _activitySet
	 */
	public Challenge(String _name, Date _startDate, Date _endDate, Set<String> _playersSet, Set<String> _activitySet)
	{
		name = _name;
		startDate = _startDate;
		endDate = _endDate;
		playersSet = _playersSet;
		activitySet = _activitySet;
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
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	@Column (name="CHALLENGE_NAME")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
		
	@Column (name="START_DATE")
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
		
	@Column (name="END_DATE")
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@ElementCollection(targetClass = String.class)
	@Column (name="PLAYER_ID")
	public Set<String> getPlayersSet() {
		return playersSet;
	}
	public void setPlayersSet(Set<String> playersSet) {
		this.playersSet = playersSet;
	}
	
	@ElementCollection(targetClass = String.class)
	@Column (name="MWBF_ACTIVITIES")
	public Set<String> getActivitySet() {
		return activitySet;
	}
	public void setActivitySet(Set<String> activitySet) {
		this.activitySet = activitySet;
	}
	
	
	@Override
	public String toString()
	{
		return "Id ["+id+"], Name["+name+"], StartDate["+startDate.toString()+"], EndDate["+endDate.toString()+"], NumberOfPlayers["+playersSet.toString()+"], NumberOfActivities["+activitySet.toString()+"], Creator[" + creator.getEmail() + "]";
	}
}
