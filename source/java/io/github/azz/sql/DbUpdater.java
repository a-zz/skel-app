/* ****************************************************************************************************************** *
 * DbUpdater.java                                                                                                     *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import io.github.azz.logging.AppLogger;

/**
 * Online database updater. Keeps data model in sync with application releases.
 * @author a-zz
 */
public class DbUpdater {

	private static final int appDbVersion = 1;
	
	/**
	 * Checks the current database version. If it's lagging behind the current application DB version, an update process
	 * 	is launched automagically.
	 * @param unattended (boolean) Sets wether the check & update process is launched unattendedly (e.g. at application
	 * 	boot) or manually by an operator
	 * @throws SQLException
	 */
	public static void checkVersionAndUpdate(Boolean unattended) throws SQLException {
		
		AppLogger logger = new AppLogger(DbUpdater.class);
		
		int currentDbVersion = getCurrentDbVersion();
		if(currentDbVersion<appDbVersion) {
			logger.debug("Current DB version behind application DB version: " 
					+ currentDbVersion + " < " + appDbVersion + " - Updating now");
			
			for(int v = currentDbVersion+1; v<=appDbVersion; v++) {
				try {
					Method method = DbUpdater.class.getDeclaredMethod("updateToVersion" + v, Boolean.class);
					method.setAccessible(true);
					method.invoke(null, unattended);
				}
				catch(Exception e) {
					throw new SQLException("Couldn't update to version " + v + ": " + e.getMessage());
				}
			}
		}
		else if(currentDbVersion>appDbVersion) 
			throw new SQLException("Current DB version **ahead** of application DB version!!! " +
					"Checkout / update to the last application release");
		else
			logger.debug("Current DB version in sync with application: " + appDbVersion);
	}
	
	private static int getCurrentDbVersion() throws SQLException {
		
		SqlTransaction t=null;
		
		try {
			t = new SqlTransaction(true);
			String sql = "select max(VERSION) as VERSION from DBVERSION";
			ResultSet rs = t.query(sql);
			rs.next();
			return rs.getInt("VERSION");
		}
		catch(SQLSyntaxErrorException e) {
			// DBVERSION table not found: database initialization needed
			return -1;
		}
		finally {
			t.close();
		}
	}
	
	/**
	 * Functions called updateToVersion#() are called in numerical order to sync the database with the app version.<br/>
	 * 	<br/>
	 * 	E.g. if database version is 25 an app database version is 27, the update process will go through 
	 * 	updateToVersion26() and updateToVersion27() 
	 * 	<br/>
	 * 	Function updateToVersion0() is special: it does the initial population of the database; specifically, it creates 
	 * 	the DBVERSION table needed to keep track of the current version. 
	 * @param unattended (Boolean) As certain updates may require human intervention, this flags tell wether the
	 * 	update process is launched unattendedly (e.g. at application boot) o manually. An update function requiring
	 * 	human intervention will refuse to run if unattended is set to true. Otherwise, it will change unattended to true
	 *  after execution, thus forcing the update process to stop at the next function requiring intervention.
	 */
	@SuppressWarnings("unused")
	private static void updateToVersion0(Boolean unattended) throws SQLException {
	
		SqlTransaction t = null;

		try {
			t = new SqlTransaction(true);
			t.statement("create table DBVERSION "
					+ "(ID integer identity primary key, "
					+ "VERSION integer not null, "
					+ "DESCRIPTION varchar(100) not null)");
			saveVersionInfo(0, "Initialize DB version tracking", t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
			AppLogger logger = new AppLogger(DbUpdater.class);
			logger.debug("Database updated to version 0");
		}
	}
	
	@SuppressWarnings("unused")
	private static void updateToVersion1(Boolean unattended) throws SQLException {
		
		SqlTransaction t = null;
		
		try {
			t = new SqlTransaction(true);
			t.statement("create table PROPERTIES "
					+ "(UUID varchar(40) primary key, "
					+ "KEY varchar(50) not null, "
					+ "VALUE clob not null, "
					+ "CREATED timestamp default localtimestamp not null, "
					+ "MODIFIED timestamp default localtimestamp not null, "
					+ "READ timestamp)");
			saveVersionInfo(1, "Application configuration support", t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	private static void saveVersionInfo(int version, String description, SqlTransaction t) throws SQLException {
	
		t.statement("insert into DBVERSION (VERSION, DESCRIPTION) "
				+ "values (" + version + ", '" + description + "')");
	}
}
/* ****************************************************************************************************************** */