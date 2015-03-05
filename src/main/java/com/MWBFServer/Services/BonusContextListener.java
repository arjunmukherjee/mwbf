package com.MWBFServer.Services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.MWBFServer.Activity.BonusEnum;
import com.MWBFServer.Activity.UserActivity;
import com.MWBFServer.Datasource.DataCache;
import com.MWBFServer.Users.User;
import com.MWBFServer.Utils.Constants;
import com.MWBFServer.Utils.Utils;

public class BonusContextListener implements ServletContextListener 
{
	private static final Logger log = Logger.getLogger(BonusContextListener.class);
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
	
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
		// Logic to check for bonus points
		// 1. Iterate through each user
		// 2. Check their logged activities for the week
		// 3. If the number of distinct activity types are greater then 4
		// 4. Check if the points per activity is greater than the minimum required to qualify for a bonus worthy activity
		// 5. Insert a bonus row for cross training into their activities
		// 6. Think about not interfering with clients, i.e. clients should not have to hard-code a "bonus" ignore.
		
		Runnable task = new Runnable()
		{
			@Override
			public void run() 
			{
				log.info("Checking for Cross Training bonus.");
				
				// Calculate the start and the end of the current week
		    	Calendar c = Calendar.getInstance();
		    	c.setTime(new Date());
		    	int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
		    	c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
		    	
		    	Date weekStart = c.getTime();
		    	// we do not need the same day a week after, that's why use 6, not 7
		    	c.add(Calendar.DAY_OF_MONTH, 6); 
		    	Date weekEnd = c.getTime();
		    	
		    	SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
		    	
		    	log.info("WeekStart ["+df.format(weekStart)+" 00:00:01 AM"+"], WeekEnd ["+df.format(weekEnd)+" 11:59:59 PM"+"]");
		    	
				for(User user : DataCache.getInstance().getUsers())
				{
					Set<String> activitySet = new HashSet<String>();
					
					// Get a list of user activities aggregated by activity
					List<UserActivity> activityList = Utils.getUserActivitiesByActivityForDateRange(user, df.format(weekStart)+" 00:00:01 AM", df.format(weekEnd)+" 11:59:59 PM" );
					if ( ( activityList != null ) && ( activityList.size() >= Constants.NUMBER_OF_EXERCISES_FOR_CROSS_TRAINING_BONUS ) )
					{
						for (UserActivity ua : activityList)
						{
							if ( ua.getPoints() >= Constants.POINTS_PER_EXERCISE_FOR_CROSS_TRAINING_BONUS )
								activitySet.add(ua.getActivityId());
						}
						
						if ( activitySet.size() >= Constants.NUMBER_OF_EXERCISES_FOR_CROSS_TRAINING_BONUS )
						{
							log.info("User [" + user.getFirstName() + "] is eligile for a cross training bonus this week.");
							
							UserActivity bonusActivity = new UserActivity(user,BonusEnum.CrossTrainingBonus.toString(),"1");
							bonusActivity.setPoints(BonusEnum.CrossTrainingBonus.getValue());
							log.info("Bonus ["+bonusActivity.toString()+"]");
							List<UserActivity> bonusActList = new ArrayList<UserActivity>();
							bonusActList.add(bonusActivity);
							Utils.logActivity(bonusActList);
						}
					}
				}
			}
		};
		
		// TODO : Account for Time Zones
		// Calculate the time between Now and Saturday 11pm
		Calendar calNow = Calendar.getInstance();
	    calNow.setTime(new Date());

	    int day = calNow.get(Calendar.DAY_OF_WEEK);
	    int hour = calNow.get(Calendar.HOUR_OF_DAY);
	    int currentHourInWeek = day*24 + hour;
	    int hoursUntiTakeOffOnDDay = 7*24 + Constants.HOUR_OF_DAY_TO_RUN_BONUS_CHECK;
	    int delayInHours = hoursUntiTakeOffOnDDay - currentHourInWeek;

	    // Account for the check running at Saturday 11pm
	    if ( delayInHours < 0 )
	    	delayInHours = 24*7 - (Math.abs(delayInHours));
	    
	    log.info("Starting Bonus Service in [" + delayInHours + "] hours [Saturday 11pm].");
	
	 	// Scheduled to run every Saturday at 11pm
		scheduledExecutorService.scheduleAtFixedRate(task, delayInHours, 24*7, TimeUnit.HOURS);
	}
}