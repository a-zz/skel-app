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
	
	/**
	 * Initializes the database manager facility, by reading the connection parameters from the local configuration.
	 * 	Also checks the database version and updates it if necessary.
	 * @throws SQLException
	 */
	public static void initialize() throws SQLException {
		
		try {
			url = LocalConfiguration.getProperty("db.hsqldb.url");
			usr = LocalConfiguration.getProperty("db.hsqldb.usr");
			pwd = LocalConfiguration.getProperty("db.hsqldb.pwd");
			
			Class.forName("org.hsqldb.jdbcDriver");
			DbUpdater.checkVersionAndUpdate(true);
			
			AppLogger logger = new AppLogger(DbManager.class);
			logger.debug("Database management facility initialized!");
		}
		catch(Exception e) {
			throw new SQLException("Unable to initialize the database manager: " + e.getMessage());
		}
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
}
/* ****************************************************************************************************************** */