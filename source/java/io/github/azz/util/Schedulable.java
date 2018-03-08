/* ****************************************************************************************************************** *
 * Schedulable.java                                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.sql.SQLException;
import java.util.TimerTask;

import io.github.azz.config.AppConfiguration;
import io.github.azz.logging.AppLogger;

/**
 * Abstract super-class for scheduled tasks
 */
public abstract class Schedulable extends TimerTask {
	
	/**
	 * Sets whether the task is enabled. Default is false. 
	 */
	protected Boolean enabled = false;

	/**
	 * Sets the time of the first run for the task. Default is 22:00:00.
	 */
	protected String startTime = "00:00:00";
	
	/**
	 * Sets the recurrency period for the task (in milliseconds). Default is 1 hour.
	 */
	protected Long period = new Long(60 * 60 * 1000);

	/**
	 * Gets the setup for a scheduled task. Setup is stored as an application configuration property, which key is 
	 * 	the class name. If the property doesn't exist, the default values are used.
	 * @throws SQLException
	 */
	protected void getSetup() throws SQLException {
		
		AppLogger logger = new AppLogger(this.getClass());
		
		String taskSetup = AppConfiguration.getProperty(this.getClass().getName());
		if(taskSetup!=null) {
			String[] setup = taskSetup.split(";");
			enabled = new Boolean(setup[0]);
			startTime = setup[1];
			period = new Long(setup[2]);
			logger.debug("Setup found for task: " + taskSetup);
		}
		else
			logger.debug("Setup not found for task. Using defaults.");
	}
	
	/**
	 * Saves the current task setup as an application property which key is the class name.
	 * @throws SQLException
	 * @see getSetup()
	 */
	protected void saveSetup() throws SQLException {
		
		String value = enabled.toString() + ";" + startTime + ";" + Long.toString(period);
		AppConfiguration.setProperty(this.getClass().getName(), value);
	}
	
	public abstract void run();
	
	/**
	 * The action to be performed at scheduler shutdown
	 */
	public abstract void lastRun();
}
/* ****************************************************************************************************************** */