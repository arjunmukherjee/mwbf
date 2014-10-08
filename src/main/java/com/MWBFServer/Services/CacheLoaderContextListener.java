package com.MWBFServer.Services;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.DataCache;

public class CacheLoaderContextListener implements ServletContextListener 
{
	private static final Logger log = Logger.getLogger(BonusContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
		log.info("CacheLoader stopped.");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run() 
			{
				log.info("Starting Cache Loader.");
				DataCache.getInstance().loadData();
			}
		};
		
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();		
	}
}