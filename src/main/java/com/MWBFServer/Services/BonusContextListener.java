package com.MWBFServer.Services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class BonusContextListener implements ServletContextListener 
{
	private static final Logger log = Logger.getLogger(BonusContextListener.class);
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() 
	{
		@Override
		public Thread newThread(Runnable runnable) 
		{
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			//thread.setDaemon(true);
			return thread;
		}
	});
	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
		// TODO : Cleanup
		log.info("BonusService stopped.");
		scheduledExecutorService.shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		// TODO Implement logic to check for bonus points
		// 1. Iterate through each user
		// 2. Check their logged activities for the week
		// 3. If the number of distinct activity types are greater then 4
		// 4. Insert a bonus row for cross training into their activities
		// 5. Think about not interfering with clients, i.e. clients should not have to hard-code a "bonus" ignore.
		
		log.info("BonusService started.");
		Runnable task = new Runnable()
		{
			@Override
			public void run() 
			{
				log.info("Checking for Cross Training bonus.");
			}
		};
		
		// TODO : Schedule to run every Saturday at 9pm
		scheduledExecutorService.scheduleAtFixedRate(task, 1, 30, TimeUnit.SECONDS);
	}
}