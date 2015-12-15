package com.MWBFServer.Services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Datasource.CacheManager;
import com.MWBFServer.Utils.Constants;

public class CacheUpdaterContextListener implements ServletContextListener
{
	private static final Logger log = Logger.getLogger(BonusContextListener.class);
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private static final BlockingQueue<UserActivity> taskQueue = new LinkedBlockingQueue<UserActivity>();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
		log.info("Shutting down the CACHE UPDATER.");
		executorService.shutdown();
	}
	
	/**
	 * Method used to add tasks to the task blocking queue.<br>
	 * Access to blocking queues are inherently thread safe.<br>
	 * TODO : Create an interface called CacheUpdaterTask and make UserActivity implement it.
	 * @param _ua
	 */
	public static void addTask(UserActivity _ua)
	{
		taskQueue.add(_ua);
	}

	/**
	 * Every-time a new activity is added to the queue, it is processed by this thread pool. <br>
	 * The activity is added to the users activity cache and also to the feeds cache.
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		// Create a new thread to keep checking the blocking queue
		// Once an activity is found in the queue, use one of the threads from the pool to update the cache
		Runnable cacheUpdaterTask = new Runnable()
		{
			@Override
			public void run() 
			{
				log.info("Started CACHE UPDATER");
				while (true)
				{
					try 
					{
						// Waiting for an activity to be submitted to the queue
						final UserActivity ua = taskQueue.take();
						executorService.submit(new Runnable() 
						{
							@Override
							public void run() 
						    {
								CacheManager.getCache().addUserActivity(ua);
						    }
						});
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
		};
		
		// The main cache updater thread (and task)
		Thread cacheUpdaterThread = new Thread(cacheUpdaterTask);
		cacheUpdaterThread.setName(Constants.CACHE_UPDATER_THREAD_NAME);
		cacheUpdaterThread.start();
	}

}
