package com.MWBFServer.Services;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.MWBFServer.Datasource.SimpleCache;

public class CacheLoaderContextListener implements ServletContextListener 
{
	private static final Logger log = Logger.getLogger(CacheLoaderContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) 
	{
		log.info("CacheLoader stopped.");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		log.info("Starting Cache Loader.");
		
		// Will init the single instance, which will in turn load the cache
		SimpleCache.getInstance();
	}
}