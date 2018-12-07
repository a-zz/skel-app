/* ****************************************************************************************************************** *
 * DbManagerHSQLDB.java                                                                                               *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql.da;

import java.io.IOException;
import java.sql.SQLException;

import io.github.azz.config.LocalConfiguration;
import io.github.azz.sql.SqlTransaction;
import io.github.azz.sql.rdbms.HSQLDBInterface;

/**
 * HSQLDB data access implementation for DbManager class 
 * @author a-zz
 */
public class DbManagerHSQLDB implements DbManagerDaInterface, HSQLDBInterface {

	public boolean checkDbTimeSync() throws SQLException {
		
		// Embedded database: synchronization can be took for sure
		return true;
	}

	public void shutdownEngine() throws SQLException {
		
		SqlTransaction t = null;
		try {
			t = new SqlTransaction("hsqldb engine shutdown", true);
			boolean compactDatabase = false;
			try {
				compactDatabase = LocalConfiguration.getProperty("db.hsqldb.compactOnShutdown").equals("y"); 
			}
			catch(IOException e) {
				// Do nothing, won't compact
			}
			t.statement("shutdown" + (compactDatabase?" compact":""));			
		}
		finally {
			t.close();				
			
		}
	}
}
/* ****************************************************************************************************************** */