/* ****************************************************************************************************************** *
 * DbUpdaterDaInterface.java                                                                                          *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql.da;

import java.sql.SQLException;

import io.github.azz.sql.DaInterface;

/**
 * Data access interface for DbUpdater class
 * @author a-zz
 */
public interface DbUpdaterDaInterface extends DaInterface {

	/**
	 * Gets the current database version
	 * @return (int)
	 * @throws SQLException
	 */
	public int getCurrentDbVersion() throws SQLException;
	
	/**
	 * Functions called updateToVersion#() are invoked in numerical order to bring the database version up to the app 
	 * 	version.
	 * 	<br/><br/>
	 * 	E.g. if database version is 25 and app database version is 27, the update process will go through 
	 * 	updateToVersion26() and updateToVersion27() 
	 * 	<br/><br/>
	 * 	Database versions aren't always incremented with app code commits. Only those commits requiring data model 
	 * 	alterations should (would, must) increment the database version.
	 * 	<br/><br/>
	 * 	Function updateToVersion0() is special: it does the initial population of the database; among other things, it 
	 * 	creates the DBVERSION table needed to keep track of the current version. 
	 * @param unattended (Boolean) As certain updates may require human intervention, this flags tell wether the
	 * 	update process is launched unattendedly (e.g. at application boot) o manually. An update function requiring
	 * 	human intervention will refuse to run if unattended is set to true. Otherwise, it will change unattended to true
	 *  after execution, thus forcing the update process to stop at the next function requiring intervention.
	 */
	public void updateToVersion0(Boolean unattended) throws SQLException;
	
	//public void updateToVersion1(Boolean unattended) throws SQLException;
	//public void updateToVersion2(Boolean unattended) throws SQLException;
	//public void updateToVersion3(Boolean unattended) throws SQLException;
	//(etc.)
}
/* ****************************************************************************************************************** */