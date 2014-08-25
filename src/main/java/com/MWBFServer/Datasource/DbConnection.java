package com.MWBFServer.Datasource;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Datasource.DBReturnClasses.UserActivityByTime;
import com.MWBFServer.Stats.PersonalStats;
import com.MWBFServer.Users.Friends;
import com.MWBFServer.Users.User;

@SuppressWarnings("deprecation")
public class DbConnection 
{
	private static final Logger log = Logger.getLogger(DbConnection.class);
	private static SessionFactory sessionFactory;
	
	static
	{
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}
	
	private DbConnection(){}
	
	private static Session getSession()
	{
		return sessionFactory.openSession();
	}
	
	/**
	 * Save an object to the DB.
	 * @param _o
	 * @return
	 */
	public static Boolean saveObj(Object _o)
	{
		Session s = getSession();
		try
		{
			s.beginTransaction();
			s.save(_o);
			s.getTransaction().commit();
		}
		catch(Exception e)
		{
			log.error("Execption during saving : " + e.getMessage());
			s.getTransaction().rollback();
			return false;
		}
		finally
		{
			s.close();
		}
		
		return true;
	}
	
	/**
	 * Save a list of objects to the DB.
	 * @param _objList
	 * @return
	 */
	public static Boolean saveList(List<?> _objList)
	{
		Session s = getSession();
		try
		{
			s.beginTransaction();
			
			for (Object o : _objList)
				s.save(o);
			
			s.getTransaction().commit();
		}
		catch(Exception e)
		{
			log.error("Execption during saving : " + e.getMessage());
			s.getTransaction().rollback();
			return false;
		}
		finally
		{
			s.close();
		}
		
		return true;
	}
	
	/**
	 * Returns a list of all registered users.
	 * @return
	 */
	public static List<User> queryGetUsers()
	{
		// creating session object
		Session session = getSession();
       	Query query = session.createQuery("FROM User");
      
        return (List<User>) executeListQuery(query, session);
	}
	
	/**
	 * Returns a list of all valid MWBF activities.
	 * @return
	 */
	public static List<Activities> queryGetActivityList()
	{
		// creating session object
		Session session = getSession();
       	Query query = session.createQuery("FROM Activities");
      
        return (List<Activities>) executeListQuery(query, session);
	}
	
	/**
	 * Returns a list of UserActivities aggregated by activity.
	 * @param _user
	 * @return
	 */
	public static List<UserActivity> queryGetUserActivity(User _user)
	{
		// creating session object
		Session session = getSession();
       	
		String hql = "FROM UserActivity UA WHERE UA.user = :userId";
        Query query = session.createQuery(hql);
        query.setString("userId", _user.getId());
      
        return (List<UserActivity>) executeListQuery(query, session);
	}
	
	
	/**
	 * Returns a list of UserActivities aggregated by time
	 * @param _user
	 * @param _fromDate
	 * @param _toDate
	 * @return
	 */
	public static List<?> queryGetUserActivityByTime(User _user, Date _fromDate, Date _toDate, String _dateAggregatedBy)
	{
		log.info("Getting user activities by time from [" + _fromDate.toString() + "] to [" + _toDate.toString() + "], aggregating by [" + _dateAggregatedBy + "].");
		
		// creating session object
		Session session = getSession();

		String hql = "SELECT colA, SUM(colB) FROM (SELECT date_trunc('"+_dateAggregatedBy+"',UA.activity_date) colA,SUM(UA.points) colB FROM user_activity UA";
		hql += " WHERE UA.activity_date > '"+_fromDate+"' AND UA.activity_date < '"+_toDate+"'";
		hql += " AND UA.user_id = '"+_user.getId()+"'";
		hql += " GROUP BY UA.activity_date";
		hql += " ORDER BY date_trunc('"+_dateAggregatedBy+"',UA.activity_date))sub GROUP BY colA";	
		Query query = session.createSQLQuery(hql);
	
		return executeListQuery(query,session);
    }
	
	public static List<?> queryGetUserActivityByTime(String _user, Date _fromDate, Date _toDate, String _activityList)
	{
		String hql = "select SUM(points) from user_activity where user_id='" + _user + "'";
		hql += " and activity_date > '" + _fromDate + "' and activity_date < '" + _toDate + "'";
		hql += " and activity_id in (" + _activityList + ")";
	
		return createQueryAndExecute(hql);
    }
	
	/**
	 * Executes the query and returns a generic list of the results.
	 * @param _query
	 * @param _session
	 * @return
	 */
	private static List<?> executeListQuery(Query _query, Session _session)
	{
		List<?> resultList = null;
		
		try
		{
			_session.beginTransaction();

			resultList = _query.list();
			_session.getTransaction().commit();
		}
		catch(Exception e)
		{
			log.error("Execption during select : " + e.getMessage());
			_session.getTransaction().rollback();
		}
		finally
		{
			_session.close();
		}

		return resultList;
	}

	public static Boolean deleteAllActivitiesForUser(User _user) 
	{
		// creating session object
		Session session = getSession();
		
		try
		{
			session.beginTransaction();

			String hql = "delete FROM UserActivity UA WHERE UA.user = :userId";
	        Query query = session.createQuery(hql);
	        query.setString("userId", _user.getId());
			
	        query.executeUpdate();
			session.getTransaction().commit();
		}
		catch(Exception e)
		{
			log.error("Execption during select : " + e.getMessage());
			session.getTransaction().rollback();
			return false;
		}
		finally
		{
			session.close();
		}
		
		return true;
	}

	public static List<Friends> queryGetFriendsList(User _user) 
	{
		// creating session object
		Session session = getSession();

		String hql = "FROM Friends FR WHERE FR.user = :userId";
		Query query = session.createQuery(hql);
		query.setString("userId", _user.getId());

		return (List<Friends>) executeListQuery(query, session);
	}

	/**
	 * For a given user, get a list of all the challenges the user is involved in.
	 * @param _user
	 * @return
	 */
	public static List<?> queryGetChallenges(User _user) 
	{
		String hql = "select * from mwbf_challenges a JOIN challenge_playersSet b";
		hql += " on a.id=b.challenge_id and b.player_id = '"+ _user.getId() + "'";
		
		return  createQueryAndExecute(hql);
	}
	
	/**
	 * Get the players involved in the challenged identified by the set of challengeIds.
	 * 
	 * @param _user
	 * @param _ids
	 * @return
	 */
	public static List<?> queryGetChallengePlayers(String _idList) 
	{
		String hql = "select * from challenge_playersSet where challenge_id in (" + _idList + ")";
		return  createQueryAndExecute(hql);
	}
	
	/**
	 * Get a list of all the activities for the challenges identified by the idList.
	 * @param _idList
	 * @return
	 */
	public static List<?> queryGetChallengeActivities(String _idList) 
	{
		String hql = "select * from challenge_activitySet where challenge_id in (" + _idList + ")";
		return  createQueryAndExecute(hql);
	}
	
	/**
	 * Get the sum of points per player for specific date ranges.
	 * @param _userId
	 * @param _startDate
	 * @param _endDate
	 * @return
	 */
	public static List<?> queryGetPlayerPointsByDate(String _userId, Date _startDate, Date _endDate) 
	{
		String hql = "select SUM(points),user_id from user_activity where user_id='"+ _userId +"'";
		hql += " and activity_date > '8/18/2014' and activity_date < '8/20/2014' group by user_id";
				
		return  createQueryAndExecute(hql);
	}
	
	/**
	 * Create the query and execute it.
	 * @param _query
	 * @return
	 */
	private static List<?> createQueryAndExecute(String _query)
	{
		// creating session object
		Session session = getSession();

		Query query = session.createSQLQuery(_query);

		return  executeListQuery(query, session);
	}

	
	/**
	 * Get the users personal stats (weight, body fat percentage etc)
	 * @param _user
	 * @return
	 */
	public static List<PersonalStats> queryGetPersonalStats(User _user) 
	{
		// creating session object
		Session session = getSession();

		String hql = "FROM PersonalStats PS WHERE PS.user = :userId";
		Query query = session.createQuery(hql);
		query.setString("userId", _user.getId());

		return (List<PersonalStats>) executeListQuery(query, session);
	}


	/**
	 * Retrieve the users all time bestDay, bestMonth and bestYear
	 * @param _user
	 */
	public static List<UserActivityByTime> queryGetAllTimeHighs(User _user, String _aggregateBy)
	{
		String hql = "SELECT colA, SUM(colB) FROM (SELECT date_trunc('" + _aggregateBy + "',UA.activity_date) colA,SUM(UA.points) colB";
		hql += " FROM user_activity UA WHERE UA.user_id = '" + _user.getId() + "'";
		hql += " GROUP BY UA.activity_date ORDER BY date_trunc('" + _aggregateBy + "',UA.activity_date))sub GROUP BY colA";
				
		return (List<UserActivityByTime>) createQueryAndExecute(hql);
	}

	

}
