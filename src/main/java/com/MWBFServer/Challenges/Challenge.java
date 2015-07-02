package com.MWBFServer.Challenges;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.BasicUtils;

@Entity
@Table (name="MWBF_CHALLENGES")
public class Challenge implements Serializable
{
	private static final long serialVersionUID = 4920633576979574822L;
	private static final Logger log = Logger.getLogger(Challenge.class);
	private static final CacheManager m_cache = BasicUtils.getCache();
	
	private long id;
	private User creator;
	private String winnerId;
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
	
	public String getWinnerId() 
	{
		return winnerId;
	}
	public void setWinnerId(String _winnerId) 
	{
		this.winnerId = _winnerId;
	}
	
	
	@Override
	public String toString()
	{
		return "Id ["+id+"], Name["+name+"], Winner[" + winnerId + "], StartDate["+startDate.toString()+"], EndDate["+endDate.toString()+"], NumberOfPlayers["+playersSet.toString()+"], NumberOfActivities["+activitySet.toString()+"], Creator[" + creator.getEmail() + "]";
	}
	
	/**
	 * Add a new challenge.
	 * @param _newChallenge
	 * @return
	 */
	public static boolean addChallenge(Challenge _newChallenge) 
	{
		boolean result = DbConnection.saveObj(_newChallenge);
		if ( result )
			m_cache.addChallenge(_newChallenge);
		
		return result;
	}
	
	/**
	 * First find the challenge object.
	 * Delete the challenge object.
	 * @param _challengeId
	 * @return
	 */
	public static boolean deleteChallenge(String _challengeId) 
	{
		boolean success = true;
	
		@SuppressWarnings("unchecked")
		List<Challenge> challengeList = (List<Challenge>) DbConnection.queryGetChallenge(_challengeId);
		if ( ( challengeList != null ) && ( challengeList.size() > 0 ) )
		{
			Challenge challenge = challengeList.get(0);
			
			if (challenge != null )
				success = DbConnection.deleteChallenge(challenge);
			else
				success = false;
			
			if ( success )
			{
				// TODO Delete the challenge from the cache
				//m_cache.deleteChallenge(challenge)
			}
		}
		else
		{
			log.warn("Could not find the challenge with Id [" + _challengeId + "]");
			success = false;
		}
		
		return success;
	}

}
