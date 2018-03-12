/* ****************************************************************************************************************** *
 * AppConfiguration.java                                                                                              *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config;

import java.sql.SQLException;

import io.github.azz.config.da.AppConfigurationDaInterface;
import io.github.azz.logging.AppLogger;
import io.github.azz.sql.DaInterface;
import io.github.azz.sql.SqlTransaction;

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
		logger.trace("Property " + key + " " + (value!=null?"read":"not found"));
		return value;
	}
	
	/**
	 * Gets a property value, using an already-running SQL transaction (and thus honoring its isolation level).
	 * @param key (String) The property key
	 * @param t (SqlTransaction) The transaction to be used.
	 * @return (String) The property value. null if the property doesn't exist.
	 * @throws SQLException
	 */
	public static String getProperty(String key, SqlTransaction t) throws SQLException {
		
		String value = dao.getProperty(key, t);
		logger.trace("Property " + key + " " + (value!=null?"read":"not found"));
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
		logger.trace("Property " + key + " set");
	}
	
	/**
	 * Sets a property, using an already-running SQL transaction (and thus honoring its isolation level). If the 
	 * property doesn't previously exist, it's created on the fly.
	 * @param key (String) The property key.
	 * @param t (SqlTransaction) The transaction to be used.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @throws SQLException
	 */
	public static void setProperty(String key, String value, SqlTransaction t) throws SQLException {
		
		dao.setProperty(key, value, t);
		logger.trace("Property " + key + " set");
	}
	
	/**
	 * Deletes a property
	 * @param key (String) The property key
	 * @throws SQLException
	 */
	public static void deleteProperty(String key) throws SQLException {
			
		dao.deleteProperty(key);
		logger.trace("Property " + key + " deleted");
	}
	
	/**
	 * Deletes a property, using an already-running SQL transaction (and thus honoring its isolation level).
	 * @param key (String) The property key
	 * @param t (SqlTransaction) The transaction to be used.
	 * @throws SQLException
	 */
	public static void deleteProperty(String key, SqlTransaction t) throws SQLException {
			
		dao.deleteProperty(key, t);
		logger.trace("Property " + key + " deleted");
	}	
}
