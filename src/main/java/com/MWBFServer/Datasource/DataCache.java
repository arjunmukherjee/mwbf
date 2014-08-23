package com.MWBFServer.Datasource;

public class DataCache 
{
	private static DataCache singleInstance;
	
	/**
	 * Singleton class, to cache the data in memory for quick access
	 */
	private DataCache()
	{
		
	}
	
	/**
	 * Returns the single cache instance (@NotThreadSafe)
	 * @return
	 */
	public static DataCache getInstance()
	{
		if (singleInstance == null)
			singleInstance = new DataCache();
		
		return singleInstance;
	}

}
