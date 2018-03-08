/* ****************************************************************************************************************** *
 * Scheduler.java                                                                                                     *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Timer;

import io.github.azz.logging.AppLogger;

/**
 * Task scheduler, for recurrent unattended operations run at fixed-rate. See doc/scheduler.txt for more info.
 * @author a-zz
 */
public class Scheduler {

	private static AppLogger logger = new AppLogger(Scheduler.class);
	private static final String scheduledTaskPackageName = "io.github.azz.util.scheduled";
	private static Timer timer;	
	private static ArrayList<Schedulable> scheduledTaskList;
	
	/**
	 * Initializes the task scheduler
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public static void initialize() 
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
		 
		timer = new Timer();
		scheduledTaskList = new ArrayList<Schedulable>();
		
		// Looks for schedulable tasks
		ArrayList<String> foundTaskClassesByName = new ArrayList<String>();
		Reflection.scanPackage(scheduledTaskPackageName, false, foundTaskClassesByName, Schedulable.class.getName());
		for(String taskClassName : foundTaskClassesByName) {
			// Instantiate task and add to timer
			@SuppressWarnings("unchecked")
			Class<? extends Schedulable> taskClass = (Class<? extends Schedulable>)Class.forName(taskClassName);
			Schedulable task = taskClass.newInstance();
			task.getSetup();
			if(task.enabled) {
				GregorianCalendar firstRun = new GregorianCalendar();
				String[] timeElmnts = task.startTime.split(":");
				firstRun.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(timeElmnts[0]));
				firstRun.set(GregorianCalendar.MINUTE, Integer.parseInt(timeElmnts[1]));
				firstRun.set(GregorianCalendar.SECOND, Integer.parseInt(timeElmnts[2]));
				if(firstRun.compareTo(new GregorianCalendar())<0)
					firstRun.add(GregorianCalendar.DAY_OF_MONTH, 1);
				timer.scheduleAtFixedRate(task, firstRun.getTime(), task.period);
				scheduledTaskList.add(task);
				logger.debug("Task " + taskClassName + " scheduled to be run " +
						"from " + task.startTime + " every " + task.period + "ms");
			}
			else
				logger.debug("Task " + taskClassName + " is disabled; won't be scheduled");		
		}
		logger.debug("Task scheduler initialized! (" + scheduledTaskList.size() + " tasks)");
	}
	
	/**
	 * Shutdowns the task scheduler. This should always be called upon application shutdown, giving every scheduled 
	 * 	task the chance for a last run.
	 */
	public static void shutdown() {
		
		for(Schedulable task : scheduledTaskList)
			task.lastRun();
		timer.cancel();
		timer.purge();
		scheduledTaskList.clear();
	}
}
/* ****************************************************************************************************************** */