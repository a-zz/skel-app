/* ****************************************************************************************************************** *
 * AppConfigurationDaInterface.java                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config.da;

import java.sql.SQLException;

import io.github.azz.sql.DaInterface;

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
	 * Sets a property. If the property doesn't previously exists, it's created on the fly.
	 * @param key (String) The property key.
	 * @param value (String) The property value. null values are not allowed (but empty strings are).
	 * @throws SQLException
	 */
	public void setProperty(String key, String value) throws SQLException;
	
	/**
	 * Deletes a property
	 * @param key (String) The property key
	 * @throws SQLException
	 */
	public void deleteProperty(String key) throws SQLException;
}
/* ****************************************************************************************************************** */