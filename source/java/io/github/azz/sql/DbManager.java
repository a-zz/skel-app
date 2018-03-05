/* ****************************************************************************************************************** *
 * DbManager.java                                                                                                     *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.azz.config.LocalConfiguration;
import io.github.azz.logging.AppLogger;

/**
 * Database management
 * @author a-zz
 */
public class DbManager {
	
	private static String url;
	private static String usr;
	private static String pwd;
	
	public enum enumDatabaseEngines { HSQLDB };
	private static enumDatabaseEngines databaseEngine;
	
	/**
	 * Initializes the database manager facility, by reading the connection parameters from the local configuration.
	 * 	Also checks the database version and updates it if necessary.
	 * @throws SQLException
	 */
	public static void initialize() throws SQLException {
		
		try {
			url = LocalConfiguration.getProperty("db.url");
			usr = LocalConfiguration.getProperty("db.usr");
			pwd = LocalConfiguration.getProperty("db.pwd");
			
			registerDbDriver(url);
			DbUpdater.checkVersionAndUpdate(true);
			
			AppLogger logger = new AppLogger(DbManager.class);
			logger.debug("Database management facility initialized!");
		}
		catch(Exception e) {
			throw new SQLException("Unable to initialize the database manager: " + e.getMessage());
		}
	}
	
	/**
	 * Returns the database engine used.
	 * @return (enumDatabaseEngine)
	 */
	public static enumDatabaseEngines getDatabaseEngine() {
		
		return databaseEngine;
	}
	
	/**
	 * Get a new database connection. This connection is untracked, so it's advisable not to be used directly but by 
	 * 	means of the SqlConnection class
	 * @return
	 * @throws SQLException
	 * @see {@link SqlConnection}
	 */
	public static Connection getConnection() throws SQLException { 
		
		return DriverManager.getConnection(url, usr, pwd);
	} 
	
	/**
	 * Register the database driver class for the database URL provided. This method must be modified when adding 
	 * 	support for new database engines.
	 * @param url (String) The database URL provided. 
	 * @throws ClassNotFoundException
	 * @throws {@link UnsupportedOperationException}
	 */
	private static void registerDbDriver(String url) throws ClassNotFoundException, UnsupportedOperationException {
		
		if(url.startsWith("jdbc:hsqldb")) {
			Class.forName("org.hsqldb.jdbcDriver");
			databaseEngine = enumDatabaseEngines.HSQLDB;
		}
		else
			throw new UnsupportedOperationException("Database support not implemented for URL: " + url);
	}		
}
/* ****************************************************************************************************************** */