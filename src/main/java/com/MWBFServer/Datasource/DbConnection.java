package com.MWBFServer.Datasource;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.MWBFServer.Activity.Activities;
import com.MWBFServer.Activity.UserActivity;
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
	public static List<?> queryGetUserActivityByTime(User _user, Date _fromDate, Date _toDate)
	{
		String dateAggregatedBy = "month";
		long diffInMillies = _toDate.getTime() - _fromDate.getTime();
		if ( TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS) > 350 )
			dateAggregatedBy = "month";
		else if ( TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS) > 1  )
			dateAggregatedBy = "day";
		else 
			dateAggregatedBy = "hour";
		
		log.info("Getting user activities by time from [" + _fromDate.toString() + "] to [" + _toDate.toString() + "], aggregating by [" + dateAggregatedBy + "].");
		
		// creating session object
		Session session = getSession();

		//SELECT SUM(UA.points), date_trunc('month',UA.activity_date) FROM user_activity UA
		//WHERE UA.activity_date > '01/01/2014' AND UA.activity_date < '12/31/2014' 
		//GROUP BY date_trunc('month',UA.activity_date)
		//ORDER BY date_trunc('month',UA.activity_date)
		
		String hql = "SELECT SUM(UA.points), date_trunc(:aggregateBy,UA.date) FROM UserActivity UA";
		hql += " WHERE UA.date > :fromDate AND UA.date < :toDate GROUP BY date_trunc(:aggregateBy,UA.date),UA.date";
		hql += " ORDER BY date_trunc(:aggregateBy,UA.date)";
		Query query = session.createQuery(hql);
		query.setDate("fromDate", _fromDate);
		query.setDate("toDate", _toDate);
		query.setString("aggregateBy", dateAggregatedBy);

		log.info("Query : [" + query.getQueryString() + "]");
		
		return executeListQuery(query,session);
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
}
