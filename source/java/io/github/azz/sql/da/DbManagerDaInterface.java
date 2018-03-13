/* ****************************************************************************************************************** *
 * DbManagerDaInterface.java                                                                                          *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql.da;

import java.sql.SQLException;

import io.github.azz.sql.DaInterface;

/**
 * Data access interface for DbManager clas
 * @author a-zz
 */
public interface DbManagerDaInterface extends DaInterface {

	/**
	 * Predefined tolerance for database and application server time synchronization, in milliseconds
	 */
	public static long timeSyncTolerance = 1000;
	
	/**
	 * Checks wether the database time is in sync with the application server time. 
	 * @return (boolean) true if database and server time are synchronized (within predefined tolerance).
	 * @throws SQLException
	 * @
	 */
	public boolean checkDbTimeSync() throws SQLException;
	
	/**
	 * Shuts down the database engine. This is only needed for embedded databases (e.g. HSQLDB); otherwise, does nothing
	 * @param databaseEngine (EnumDatabaseEngines) The database engine to shut down.
	 * @throws SQLException
	 */
	public void shutdownEngine() throws SQLException;
}
/* ****************************************************************************************************************** */