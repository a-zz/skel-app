/* ****************************************************************************************************************** *
 * DbUpdater.java                                                                                                     *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.lang.reflect.Method;
import java.sql.SQLException;

import io.github.azz.logging.AppLogger;
import io.github.azz.sql.da.DbUpdaterDaInterface;

/**
 * Online database updater. Keeps data model in sync with application releases.
 * @author a-zz
 */
public class DbUpdater {

	private static final int appDbVersion = 1;
	private static AppLogger logger;
	private final static DbUpdaterDaInterface dao = init();
	
	/**
	 * Runs initialization code:
	 * <ol>
	 * <li>Instantiates logger object for class</li>
	 * <li>Instantiate data access object for class and current database engine</li>
	 * </ol>
	 * @return 
	 */
	private static DbUpdaterDaInterface init() {
	
		try {
			logger = new AppLogger(DbUpdater.class);
			return (DbUpdaterDaInterface)DaInterface.getImplClassFor(DbUpdater.class).newInstance();
		}
		catch(Exception e) {
			logger.error("Unable to instantiate data access implementation class for " + DbUpdater.class);
			return null;
		}
	}
	
	/**
	 * Checks the current database version. If it's lagging behind the current application DB version, an update process
	 * 	is launched automagically.
	 * @param unattended (boolean) Sets wether the check & update process is launched unattendedly (e.g. at application
	 * 	boot) or manually by an operator
	 * @throws SQLException
	 */
	public static void checkVersionAndUpdate(Boolean unattended) throws SQLException {
		
		int currentDbVersion = dao.getCurrentDbVersion();
		if(currentDbVersion<appDbVersion) {
			logger.debug("Current DB version behind application DB version: " 
					+ currentDbVersion + " < " + appDbVersion + " - Updating now");
			
			for(int v = currentDbVersion+1; v<=appDbVersion; v++) {
				try {
					Method method = dao.getClass().getMethod("updateToVersion" + v, Boolean.class);
					method.invoke(dao, unattended);
					logger.debug("Database updated to version " + v);
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
}
/* ****************************************************************************************************************** */