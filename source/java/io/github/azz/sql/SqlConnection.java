/* ****************************************************************************************************************** *
 * SqlConnection.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import io.github.azz.logging.AppLogger;

/**
 * A SQL database connection, with usage tracking
 * @author a-zz
 */
public class SqlConnection {

	// -- Connection tracking
	private static int serial = 0;
	private static Hashtable<Integer,SqlConnection> openConnTable = new Hashtable<Integer,SqlConnection>();
	
	// -- Connection parameters	
	private Connection conn;
	private Integer connSerial;
	private AppLogger logger = new AppLogger(SqlConnection.class);
	
	/**
	 * Get a new database connection and add it to the open connection table, so it can be tracked along its lifecycle.
	 * @param autoCommit (boolean) Set the autocommit mode for the connection.
	 * @throws SQLException
	 */
	public SqlConnection(boolean autoCommit) throws SQLException {
		
		connSerial = new Integer(getSerial());
		conn = DbManager.getConnection();
		conn.setAutoCommit(autoCommit);		
		openConnTable.put(connSerial, this);
		logger.trace("Got new SQL connection #" + connSerial);
	}
	
	/**
	 * Closes the connection. Possibly uncommited changes are rolled back.
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		
		// Discard any pending operations
		if(!conn.getAutoCommit())
			conn.rollback();
		
		// Close the connection and remove tracking info
		conn.close();
		conn = null;
		openConnTable.remove(connSerial);
		logger.trace("Closed connection #" + connSerial);
	}
	
	/**
	 * Returns the serial number for the connection (useful for logging purposes).
	 * @return (Integer) 
	 */
	public Integer getConnSerial() {
		
		return connSerial;
	}
	
	/**
	 * Returns the native Java connection object. 
	 * @return (Connection)
	 */
	public Connection getConnection() {
		
		return conn;
	}
	
	protected void finalize() throws Throwable {
		
		if(conn!=null)
			logger.warn("Check resource usage: connection #" + connSerial + " was automatically purged");
		super.finalize();
	}
	
	private synchronized int getSerial() {
		
		return serial++;
	}
}
/* ****************************************************************************************************************** */