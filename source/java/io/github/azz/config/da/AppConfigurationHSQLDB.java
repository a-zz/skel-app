/* ****************************************************************************************************************** *
 * AppConfigurationHSQLDB.java                                                                                        *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config.da;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import io.github.azz.sql.DbReplicator;
import io.github.azz.sql.SqlTransaction;
import io.github.azz.sql.rdbms.HSQLDBInterface;

/**
 * HSQLDB data access implementation for AppConfiguration class
 * @author a-zz
 */
public class AppConfigurationHSQLDB implements AppConfigurationDaInterface, HSQLDBInterface {

	public String getProperty(String key) throws SQLException {
		
		SqlTransaction t = null;
		
		try {
			t = new SqlTransaction("get app property", true);
			return getProperty(key, t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	public String getProperty(String key, SqlTransaction t) throws SQLException {
		
		ResultSet rs = t.query("select UUID, VALUE from PROPERTIES where KEY='" + key + "'");
		if(!rs.next())
			return null;
		else {
			t.statement("update PROPERTIES set READ=localtimestamp where UUID='" + rs.getString("UUID") + "'");
			return rs.getString("VALUE");
		}
	}
	
	public void setProperty(String key, String value) throws SQLException {
		
		SqlTransaction t = null;
		
		try {
			t = new SqlTransaction("set app property", true);
			setProperty(key, value, t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	public void setProperty(String key, String value, SqlTransaction t) throws SQLException {
		
		ArrayList<Object> values = new ArrayList<Object>();
		values.add(value);
		
		// First try to update the property
		int rows = t.preparedStatement("update PROPERTIES "
				+ "set VALUE=?, "
				+ "MODIFIED=localtimestamp "
				+ "where KEY='" + key + "'"
				, values);
		if(rows==1) 
			return;
		
		// No rows affected: property doesn't exist: creating now
		t.preparedStatement("insert into PROPERTIES (UUID, KEY, VALUE, CREATED, MODIFIED) values ("
				+ "'" + DbReplicator.getUUID() + "', "
				+ "'" + key + "', "
				+ "?, localtimestamp, localtimestamp)", values);		
	}
	
	public void deleteProperty(String key) throws SQLException {
			
		SqlTransaction t = null;
			
		try {
			t = new SqlTransaction("delete app property", true);
			deleteProperty(key, t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
	
	public void deleteProperty(String key, SqlTransaction t) throws SQLException {
		
		t.statement("delete from PROPERTIES where key='" + key + "'");
	}
}
/* ****************************************************************************************************************** */