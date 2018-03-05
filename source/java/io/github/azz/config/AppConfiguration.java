/* ****************************************************************************************************************** *
 * AppConfiguration.java                                                                                              *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config;

import java.sql.SQLException;

import io.github.azz.config.da.AppConfigurationDaInterface;
import io.github.azz.logging.AppLogger;
import io.github.azz.sql.DaInterface;

/**
 * Application configuration properties (database-stored)
 * @author a-zz
 */
public class AppConfiguration {
	
	private static AppLogger logger;
	private final static AppConfigurationDaInterface dao = init();
	
	/**
	 * Runs initialization code:
	 * <ol>
	 * <li>Instantiates logger object for class</li>
	 * <li>Instantiate data access object for class and current database engine</li>
	 * </ol>
	 * @return 
	 */
	private static AppConfigurationDaInterface init() {
	
		try {
			logger = new AppLogger(AppConfiguration.class);
			return (AppConfigurationDaInterface)DaInterface.getImplClassFor(AppConfiguration.class).newInstance();
		}
		catch(Exception e) {
			logger.error("Unable to instantiate data access implementation class for " + AppConfiguration.class);
			return null;
		}
	}

	/**
	 * Gets a property value
	 * @param key (String) The property key
	 * @return (String) The property value. null if the property doesn't exist.
	 * @throws SQLException
	 */
	public static String getProperty(String key) throws SQLException {
		
		String value = dao.getProperty(key);
		logger.debug("Property " + key + " read");
		return value;
	}
	
	/**
	 * Sets a property. If the property doesn't previously exists, it's created on the fly.
	 * @param key (String) The property key.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @throws SQLException
	 */
	public static void setProperty(String key, String value) throws SQLException {
		
		dao.setProperty(key, value);
		logger.debug("Property " + key + " set");
	}
	
	/**
	 * Deletes a property
	 * @param key (String) The property key
	 * @throws SQLException
	 */
	public static void deleteProperty(String key) throws SQLException {
			
		dao.deleteProperty(key);
		logger.debug("Property " + key + " deleted");
	}
}
