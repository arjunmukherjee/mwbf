package com.MWBFServer.Challenges;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Datasource.DbConnection;
import com.MWBFServer.Datasource.DBReturnClasses.DBReturnChallenge;
import com.MWBFServer.Datasource.DBReturnClasses.PlayerActivityData;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.BasicUtils;
import com.MWBFServer.Utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Entity
@Table (name="MWBF_CHALLENGES")
public class Challenge implements Serializable
{
	private static final long serialVersionUID = 4920633576979574822L;
	private static final Logger log = Logger.getLogger(Challenge.class);

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
			BasicUtils.getCache().addChallenge(_newChallenge);
		
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
				//BasicUtils.getCache().deleteChallenge(challenge);
			}
		}
		else
		{
			log.warn("Could not find the challenge with Id [" + _challengeId + "]");
			success = false;
		}
		
		return success;
	}
	
	
	/**
	 * Get a list of all the challenges the user is in
	 * @param _user
	 * @return
	 */
	public static List<DBReturnChallenge> getChallenges(User _user) 
	{
		// TODO : 
		// 1. Redundant code between feeds and this method
		// 2. Get activities from the cache
		
		CacheManager cache = BasicUtils.getCache();
		
		Map<String,DBReturnChallenge> challengeMap = null;
		List<Challenge> challengeList = cache.getUserChallenges(_user);
		if ( ( challengeList != null ) && ( challengeList.size() > 0 ) )
		{
			challengeMap = new HashMap<String,DBReturnChallenge>();

			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			
			// Construct a unique map of the challengeReturn objects
			for(Challenge challenge : challengeList )
			{
				// Orko --> Run : 1, Walk : 2
				// Som --> Trek : 2, Swim : 500
				Map<String, PlayerActivityData> userActivityAggregator = new HashMap<String,PlayerActivityData>();
				
				List<UserActivity> userActivityFeedList = new ArrayList<UserActivity>();

				DBReturnChallenge ch = new DBReturnChallenge(challenge.getName(),challenge.getStartDate(),challenge.getEndDate(),challenge.getActivitySet());
				ch.setCreatorId(challenge.getCreator().getId());

				List<?> userActivityList = DbConnection.queryUserActivitiesPerChallenge(challenge.getPlayersSet(),challenge.getActivitySet(),ch.getStartDate(),ch.getEndDate());
				List<String> messageList = new ArrayList<String>();
				String userActivityStr = gson.toJson(userActivityList);
				JsonArray jArray = parser.parse(userActivityStr).getAsJsonArray();
				for(JsonElement obj : jArray )
				{
					String[] activityParts = obj.toString().split(",");
					String activityId = activityParts[1].substring(1,activityParts[1].length()-1);
					String activityDateStr = activityParts[2].substring(1);
					String activityUnits = activityParts[4];
					String activityPointsStr = activityParts[5];
					Double activityPoints = Double.parseDouble(activityPointsStr);
					activityPoints = BasicUtils.round(activityPoints, 1);
					String userId = activityParts[6].substring(1,activityParts[6].length()-2);
					User user = cache.getUserById(userId);

					Date activityDate = null;
					try 
					{
						activityDate = new SimpleDateFormat("MMMM d", Locale.ENGLISH).parse(activityDateStr);
					} 
					catch (ParseException e)
					{
						e.printStackTrace();
					}

					UserActivity ua = new UserActivity( user,activityId,activityDate,activityUnits);
					
					// Construct the user --> Activity aggregation
					// Orko --> Ran : 5, Swam : 500, Trek : 45
					// Som --> Ran : 7, Bike : 500, Trek : 55
					PlayerActivityData playerActData = userActivityAggregator.get(userId);
					if ( playerActData != null  )
					{
						// Update the total points
						Double totalPts = playerActData.getTotalPoints();
						if ( totalPts != null )
							totalPts = totalPts + activityPoints;
						else
							totalPts = activityPoints;
						playerActData.setTotalPoints(totalPts);
						
						// Update the aggregate activity map
						Map<String,Double> actAggMap = playerActData.getActivityAggregateMap();
						if ( actAggMap != null )
						{
							Double actUnits = actAggMap.get(activityId);
							if ( actUnits != null)
								actUnits = actUnits + Double.parseDouble(activityUnits);
							else
								actUnits = Double.parseDouble(activityUnits);
							actAggMap.put(activityId, actUnits);
						}
						else
						{
							actAggMap = new HashMap<String,Double>();
							actAggMap.put(activityId, Double.parseDouble(activityUnits));
						}
						
						playerActData.setActivityAggregateMap(actAggMap);
					}
					else
					{
						Map<String,Double> actAggMap = new HashMap<String,Double>();
						actAggMap.put(activityId, Double.parseDouble(activityUnits));
						playerActData = new PlayerActivityData(userId, activityPoints, actAggMap);
					}
					
					userActivityAggregator.put(userId, playerActData);
					
					// Get the users activity feeds
					userActivityFeedList.add(ua);
				}
				
				// Add the users who do not have any activities during that time
				// Add the users with 0 points
				for (String userId : challenge.getPlayersSet())
				{
					if ( !userActivityAggregator.containsKey(userId) )
					{
						Map<String,Double> actAggMap = new HashMap<String,Double>();
						PlayerActivityData playerActData = new PlayerActivityData(userId, 0.001, actAggMap);
						userActivityAggregator.put(userId, playerActData);
					}
				}

				
				// Set the playerActivityData
				List<PlayerActivityData> playerActDataList = new ArrayList<PlayerActivityData>(userActivityAggregator.values());
				ch.setPlayerActivityData(playerActDataList);
				
				// Check if someone won the challenge (i.e. if the challenge is over and the winner hasn't previously been calculated)
				if ( challenge.getWinnerId() == null )
				{
					Date today = new Date();
					if ( challenge.getEndDate().before(today) )
		    		{
						String winningUserId = null;
						Double winningPoints = null;
		    			for (PlayerActivityData pa : playerActDataList)
						{
							if ( winningUserId == null )
							{
								winningUserId = pa.getUserId();
								winningPoints = pa.getTotalPoints();
							}
							else
							{
								if ( pa.getTotalPoints() > winningPoints )
								{
									winningUserId = pa.getUserId();
									winningPoints = pa.getTotalPoints();
								}
							}
						}
		    			
		    			// Update the challenge
		    			if ( winningUserId != null )
		    			{
			    			challenge.setWinnerId(winningUserId);
			    			cache.updateChallenge(challenge);
			    			log.info("Challenge [" + challenge.getName() + "] won by [" + challenge.getWinnerId() + "]");
			    		}
		    		}
				}
				else
					log.info("Challenge [" + challenge.getName() + "], Winner ["+challenge.getWinnerId()+"]");
				
				// TODO : Inefficient 
				// Gets all the activities, sorts them and then only returns the last x number of activities
				// Must be a better way to instead insert only upto x sorted
				
				// Get the list of activities and sort them by time
				Collections.sort(userActivityFeedList);

				for (UserActivity ua : userActivityFeedList)
					messageList.add(ua.constructNotificationString());

				// Return only the last 50 items
				int startIndex = 0;
				int endIndex = Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;
				if( messageList.size() > Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
					startIndex = messageList.size() - Constants.MAX_NUMBER_OF_MESSAGE_FEEDS;

				if( messageList.size() < Constants.MAX_NUMBER_OF_MESSAGE_FEEDS )
					endIndex = messageList.size();

				ch.setMessagesList(messageList.subList(startIndex, startIndex + endIndex));
				challengeMap.put(Long.toString(challenge.getId()), ch);
			}
		}

		if (challengeMap != null)
			return new ArrayList<DBReturnChallenge>(challengeMap.values());
		else
			return null;
	}
}
