package com.MWBFServer.Datasource;

import java.util.List;

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
	
	public static List<User> queryGetUsers()
	{
		// creating session object
		Session session = getSession();
        List<User> userList = null;

        try
		{
        	session.beginTransaction();
			
        	Query query = session.createQuery("FROM User");
            userList = query.list();
            session.getTransaction().commit();
     	}
		catch(Exception e)
		{
			log.error("Execption during select : " + e.getMessage());
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
        
        return userList;
	}
	
	public static List<Activities> queryGetActivityList()
	{
		// creating session object
		Session session = getSession();
        List<Activities> activityList = null;

        try
		{
        	session.beginTransaction();
			
        	Query query = session.createQuery("FROM Activities");
            activityList = query.list();
            session.getTransaction().commit();
     	}
		catch(Exception e)
		{
			log.error("Execption during select : " + e.getMessage());
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
        
        return activityList;
	}
	
	public static List<UserActivity> queryGetUserActivity(User _user)
	{
		// creating session object
		Session session = getSession();
        List<UserActivity> activityList = null;

        try
		{
        	session.beginTransaction();
			
        	String hql = "FROM UserActivity UA WHERE UA.user = :userId";
        	Query query = session.createQuery(hql);
        	query.setString("userId", _user.getId());
        	
        	activityList = query.list();
        	session.getTransaction().commit();
     	}
		catch(Exception e)
		{
			log.error("Execption during select : " + e.getMessage());
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
        
        return activityList;
	}
}
