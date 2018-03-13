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
import io.github.azz.sql.da.DbManagerDaInterface;
import io.github.azz.sql.rdbms.RdbmsSupport;

/**
 * Database management
 * @author a-zz
 */
public class DbManager {
	
	private static String url;
	private static String usr;
	private static String pwd;	
	private static RdbmsSupport.EnumDatabaseEngines databaseEngine;
	
	private static AppLogger logger = new AppLogger(DbManager.class);
	private static DbManagerDaInterface dao;
	
	/**
	 * Initializes the database manager facility, by reading the connection parameters from the local configuration.
	 * 	Also checks the database version and updates it if necessary.
	 * @throws SQLException
	 */
	public static void initialize() throws SQLException {
		
		try {
			logger = new AppLogger(DbManager.class);
			
			url = LocalConfiguration.getProperty("db.url");
			usr = LocalConfiguration.getProperty("db.usr");
			pwd = LocalConfiguration.getProperty("db.pwd");
			
			databaseEngine = RdbmsSupport.registerDriver(url);			
			dao = (DbManagerDaInterface)DaInterface.getImplClassFor(DbManager.class).newInstance();
			
			if(!dao.checkDbTimeSync())
				throw new SQLException("Database time out of sync with application server");
			DbUpdater.checkVersionAndUpdate(true);
						
			logger.debug("Database management facility initialized!");
		}
		catch(Exception e) {
			throw new SQLException("Unable to initialize the database manager: " + e.getMessage());
		}
	}
	
	/**
	 * Returns the database engine used.
	 * @return (RdbmsSupport.enumDatabaseEngines)
	 */
	public static RdbmsSupport.EnumDatabaseEngines getDatabaseEngine() {
		
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
	 * Shuts down the database manager facility
	 * @throws SQLException
	 */
	public static void shutdown() {
	
		AppLogger logger = new AppLogger(DbManager.class);
		try {
			dao.shutdownEngine();
		}
		catch(Exception e) {
				logger.error("Couldn't shut down the database management facility: " + e.getMessage());
		}
		logger.debug("Database management facility shut down!");					
	}	
}
/* ****************************************************************************************************************** */