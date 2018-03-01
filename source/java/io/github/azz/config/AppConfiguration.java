/* ****************************************************************************************************************** *
 * AppConfiguration.java                                                                                              *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import io.github.azz.logging.AppLogger;
import io.github.azz.sql.DbReplicator;
import io.github.azz.sql.SqlTransaction;

/**
 * Application configuration properties (database-stored)
 * @author a-zz
 */
public class AppConfiguration {
	
	/**
	 * Gets a property value
	 * @param key (String) The property key
	 * @return (String) The property value. null if the property doesn't exist.
	 * @throws SQLException
	 */
	public static String getProperty(String key) throws SQLException {
		
		SqlTransaction t = null;
		AppLogger logger = new AppLogger(AppConfiguration.class);
		
		try {
			t = new SqlTransaction(true);
			
			ResultSet rs = t.query("select UUID, VALUE from PROPERTIES where KEY='" + key + "'");
			if(!rs.next())
				return null;
			else {
				t.statement("update PROPERTIES set READ=localtimestamp where UUID='" + rs.getString("UUID") + "'");
				logger.debug("Property " + key + " read");
				return rs.getString("VALUE");
			}
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	/**
	 * Sets a property. If the property doesn't previously exists, it's created on the fly.
	 * @param key (String) The property key.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @throws SQLException
	 */
	public static void setProperty(String key, String value) throws SQLException {
		
		SqlTransaction t = null;
		AppLogger logger = new AppLogger(AppConfiguration.class);
		
		try {
			t = new SqlTransaction(true);
			ArrayList<Object> values = new ArrayList<Object>();
			values.add(value);
			
			// First try to update the property
			int rows = t.preparedStatement("update PROPERTIES "
					+ "set VALUE=?, "
					+ "MODIFIED=localtimestamp "
					+ "where KEY='" + key + "'"
					, values);
			if(rows==1) {
				logger.debug("Property " + key + " updated");
				return;
			}
			
			// No rows affected: property doesn't exist: creating now
			t.preparedStatement("insert into PROPERTIES (UUID, KEY, VALUE, CREATED, MODIFIED) values ("
					+ "'" + DbReplicator.getUUID() + "', "
					+ "'" + key + "', "
					+ "?, localtimestamp, localtimestamp)", values);
			logger.debug("Property " + key + " created");
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	/**
	 * Deletes a property
	 * @param key (String) The property key
	 * @throws SQLException
	 */
	public static void deleteProperty(String key) throws SQLException {
			
		SqlTransaction t = null;
		AppLogger logger = new AppLogger(AppConfiguration.class);
			
		try {
			t = new SqlTransaction(true);
			t.statement("delete from PROPERTIES where key='" + key + "'");
			logger.debug("Property " + key + " deleted");
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
}
