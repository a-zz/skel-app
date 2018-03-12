/* ****************************************************************************************************************** *
 * AppConfigurationDaInterface.java                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config.da;

import java.sql.SQLException;

import io.github.azz.sql.DaInterface;
import io.github.azz.sql.SqlTransaction;

/**
 * Data access interface por AppConfiguration class
 * @author a-zz
 */
public interface AppConfigurationDaInterface extends DaInterface {

	/**
	 * Gets a property value
	 * @param key (String) The property key
	 * @return (String) The property value. null if the property doesn't exist.
	 * @throws SQLException
	 */
	public String getProperty(String key) throws SQLException;
	
	/**
	 * Gets a property value, using an already-running SQL transaction (and thus honoring its isolation level).
	 * @param key (String) The property key
	 * @param t (SqlTransaction) The transaction to be used.
	 * @return (String) The property value. null if the property doesn't exist.
	 * @throws SQLException
	 */	
	public String getProperty(String key, SqlTransaction t) throws SQLException;
	
	/**
	 * Sets a property. If the property doesn't previously exist, it's created on the fly.
	 * @param key (String) The property key.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @throws SQLException
	 */
	public void setProperty(String key, String value) throws SQLException;
	
	/**
	 * Sets a property, using an already-running SQL transaction (and thus honoring its isolation level). If the 
	 * property doesn't previously exist, it's created on the fly.
	 * @param key (String) The property key.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @param t (SqlTransaction) The transaction to be used.
	 * @throws SQLException
	 */	
	public void setProperty(String key, String value, SqlTransaction t) throws SQLException;
	
	/**
	 * Deletes a property
	 * @param key (String) The property key
	 * @throws SQLException
	 */
	public void deleteProperty(String key) throws SQLException;

	/**
	 * Deletes a property, using an already-running SQL transaction (and thus honoring its isolation level)
	 * @param key (String) The property key
	 * @param t (SqlTransaction) The transaction to be used.
	 * @throws SQLException
	 */
	public void deleteProperty(String key, SqlTransaction t) throws SQLException;
}
/* ****************************************************************************************************************** */