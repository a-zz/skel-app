/* ****************************************************************************************************************** *
 * DbUpdaterHSQLDB.java                                                                                               *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql.da;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import io.github.azz.sql.SqlTransaction;
import io.github.azz.sql.rdbms.HSQLDBInterface;

/**
 * HSQLDB data access implementation for DbUpdater class
 * @author a-zz
 */
public class DbUpdaterHSQLDB implements DbUpdaterDaInterface, HSQLDBInterface {
	
	public int getCurrentDbVersion() throws SQLException {
		
		SqlTransaction t=null;
		
		try {
			t = new SqlTransaction("get current db version", true);
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
	
	public void updateToVersion0(Boolean unattended) throws SQLException {
		
		SqlTransaction t = null;

		try {
			t = new SqlTransaction("db update to version 0", true);
			t.statement("create table DBVERSION " +
					"(ID integer identity primary key, " +
					"VERSION integer not null, " +
					"INSTALLDATE timestamp default localtimestamp not null, " +
					"DESCRIPTION varchar(100) not null)");
			t.statement("create table PROPERTIES " +
					"(UUID varchar(40) primary key, " +
					"KEY varchar(50) not null, " +
					"VALUE clob not null, " +
					"CREATED timestamp default localtimestamp not null, " +
					"MODIFIED timestamp default localtimestamp not null, " +
					"READ timestamp)");
			saveVersionInfo(0, "Database (and version tracking) initialization", t);
		}
		catch(SQLException e) {
			throw e;
		}
		finally {
			t.close();
		}
	}
		
	public void saveVersionInfo(int version, String description, SqlTransaction t) throws SQLException {
	
		t.statement("insert into DBVERSION (VERSION, DESCRIPTION) "
				+ "values (" + version + ", '" + description + "')");
	}
}
/* ****************************************************************************************************************** */