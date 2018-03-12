/* ****************************************************************************************************************** *
 * SqlTransaction.java                                                                                                *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.time.StopWatch;

import io.github.azz.logging.AppLogger;

/**
 * Utility class for SQL Transactions
 * TODO To be tested thoroughfully!
 * @author a-zz
 */
public class SqlTransaction {

	private SqlConnection con;	
	private int n;
	private AppLogger logger;
	private ArrayList<Object> openObjects = new ArrayList<Object>();
	private ArrayList<String> sqlInstructions;	
	StopWatch watch = new StopWatch();
	
	/**
	 * Isolation levels for transactions, according to following table:
	 * <pre>
	 * Isolation Level  Dirty Read  Nonrepeatable Read  Phantom Read
	 * READ UNCOMMITTED Permitted   Permitted           Permitted
	 * READ COMMITTED   --          Permitted           Permitted
	 * REPEATABLE READ  --          --                  Permitted
	 * SERIALIZABLE     --          --                  --
	 * </pre>
	 * Being:
	 * <ul>
	 * <li>Dirty read: non-coherent data may be returned by a select (i.e. broken foreing keys)</li>
	 * <li>Nonrepeateble read: a row selected at some time may change in a later select within the transaction.</li>
	 * <li>Phantom read: a row selected once remain unchanged until the end of transaction; however, the rows
	 * 		returned by the select may change because of newly-added rows.</li>
	 * </ul>
	 */
	public enum EnumIsolationLevels {
		READ_UNCOMMITTED, 
		READ_COMMITTED, 
		REPEATABLE_READ,
		SERIALIZABLE };
	
	/**
	 * Constructor: starts a new SQL transaction with isolation level "REPEATABLE READ".
	 * @param autoCommit (boolean) Sets the transaction's autocommit mode
	 * @throws SQLException
	 */
	public SqlTransaction(boolean autoCommit) throws SQLException {
		
		this(autoCommit, EnumIsolationLevels.REPEATABLE_READ);
	}
	
	/**
	 * Constructor: starts a new SQL transaction
	 * @param autoCommit (boolean) Sets the transaction's autocommit mode
	 * @param (EnumIsolationLevels) Isolation level for the new transaction
	 * @throws SQLException
	 */
	public SqlTransaction(boolean autoCommit, EnumIsolationLevels isolationLevel) throws SQLException {
		
		this.logger = new AppLogger(this.getClass());
		con = new SqlConnection(autoCommit);	
		n = con.getConnSerial();
		logger.sql("Transaction #" + n + 
					": STARTED (autocommit: " + autoCommit + ")");
		if(!autoCommit)
			sqlInstructions = new ArrayList<String>();
	}
			
	/**
	 * Run a SQL statement, i.e. a SQL instruction not returning any data
	 * @param sql (String) The SQL instruction to run.
	 * @return (int) Number of rows affected by the statement
	 * @throws SQLException
	 */
	public int statement(String sql) throws SQLException {
		
		Statement st = null;
			
		try {
			st = con.getConnection().createStatement();
			watch.reset();
			watch.start(); 
			int rows = st.executeUpdate(sql); 
			watch.stop();
			if(con.getConnection().getAutoCommit())
				logger.sql("Transaction #" + n + 
						": -> " + sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
			else
				sqlInstructions.add(sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
			
			return rows;
		}
		catch(SQLException e) {
			rollback();
			throw e;
		}
		finally {
			try {
				if(st!=null) {
					st.close();
					st = null;
				}
			}
			catch(SQLException e) {
				logger.warn("Check resource usage: transaction #" + n + 
						": a Statement object couldn't be closed: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Run a SQL prepared (parametrized) statement
	 * @param sql (String) The SQL instruction to run.
	 * @param values (ArrayList<Object>) The list of values to be substituted in the statement
	 * @return (int) Number of rows affected by the statement
	 * @throws SQLException
	 */
	public int preparedStatement(String sql, ArrayList<Object> values) throws SQLException {
		
		PreparedStatement ps = null;
		
		try {		
			ps = con.getConnection().prepareStatement(sql);
			prepare(ps, values);
			watch.reset();
			watch.start(); 
			int rows = ps.executeUpdate(); 
			watch.stop();
			if(con.getConnection().getAutoCommit())
				logger.sql("Transaction #" + n + 
						": -> " + sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
			else
				sqlInstructions.add(sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
			
			return rows;
		}
		catch(SQLException e) {			
			rollback();
			throw e;
		}
		finally {
			try {
				if(ps!=null) {
					ps.close();
					ps = null;
				}
			}
			catch(SQLException e) {
				logger.warn("Check resource usage: transaction #" + n + 
						": a PreparedStatement object couldn't be closed: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Run a SQL query
	 * @param sql (String) The SQL query to run
	 * @return (ResultSet) The data result set returned by the query. This class tracks the result set object and closes
	 * 	it with the transaction, so it's unnecesary to close the result set explicitly.
	 * @throws SQLException
	 */
	public ResultSet query(String sql) throws SQLException {

		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = con.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			watch.reset();
			watch.start(); 
			rs = st.executeQuery(sql); 
			watch.stop();
			int rows = 0;
			if(rs.last())
			{
				rows = rs.getRow();
				rs.beforeFirst();
			}
			if(con.getConnection().getAutoCommit())
				logger.sql("Transaction #" + n + 
						": -> " + sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
			else
				sqlInstructions.add(sql + "; (" + rows + " rows; " + watch.getTime() + "ms)");
		}
		catch(SQLException e) {			
			rollback();
			try {
				if(rs!=null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(SQLException ee) {
				logger.warn("Check resource usage: transaction #" + n + 
						": a ResultSet object couldn't be closed: " + e.getMessage());
			}
			try {
				if(st!=null)
				{
					st.close();
					st = null;
				}
			}
			catch(SQLException ee) {
				logger.warn("Check resource usage: transaction #" + n + 
						": a Statement object couldn't be closed: " + e.getMessage());
			}
			throw e;
		}

		openObjects.add(0, rs);
		openObjects.add(0, st);			
		return rs;
	}
	
	/**
	 * Commits the transaction
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		
		try {
			if(con.getConnection().getAutoCommit())
				return;
			
			con.getConnection().commit();
			logger.sql("Transaction #" + n + 
					": COMMIT \\o/" + listSqlInstructions(true));
		}
		catch(SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Rolls back the transaction
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		
		try {
			if(con.getConnection().getAutoCommit())
				return;		
			
			con.getConnection().rollback();
			logger.sql("Transaction #" + n + 
					": ROLLBACK :_(" + listSqlInstructions(true));
		}
		catch(SQLException e) {
			throw e;
		}
	}	
	
	/**
	 * Closes the transaction, as well as its related objects (results sets and the like). Uncommited changes are 
	 * 	rolled back. Calling this function is key to keeping database resources, so it should <strong>always</strong> be 
	 * 	called upon transaction completion. It's advisable to place it in the finally block of the try-catch-finally
	 * 	structure holding a transaction.
	 */
	public void close() {
						
		try {
			if(sqlInstructions!=null && sqlInstructions.size()>0)
				rollback();
			
			closeRelatedObjects();
			con.close();
			con = null;
			logger.sql("Transaction #" + n +  
					": CLOSED");
		}
		catch(Exception e) {
			logger.error("transaction #" + n + 
						  ": COULDN'T BE CLOSED: " + e.getMessage());
		}
	}
	
	private void prepare(PreparedStatement ps, ArrayList<Object> values) 
			throws SQLException {
		
		int i = 1;
		Iterator<Object> it = values.iterator();
		while(it.hasNext()) {
			Object value = it.next();
			
			if(value instanceof String)
				ps.setString(i, (String)value	);
			else if(value instanceof Integer)
				ps.setInt(i, (Integer)value);
			else if(value instanceof Long)
				ps.setLong(i, (Long)value);
			else if(value instanceof Float)
				ps.setFloat(i, (Float)value);
			else if(value instanceof Double)
				ps.setDouble(i, (Double)value);
			else 
				throw new SQLException("Data type not implemneted for prepared statements");		
			
			i++;
		}			
	}
	
	private String listSqlInstructions(boolean purge) {
		
		String list = "";
		
		int i = 1;
		Iterator<String> it = sqlInstructions.iterator();
		while(it.hasNext()) {
			String sql = it.next();
			list += "\n\t[" + i + "]\t" + sql;	
			i++;
		}
		if(purge)
			sqlInstructions.clear();
		
		return list;
	}
	
	private void closeRelatedObjects() throws SQLException {

		Iterator<Object> it = openObjects.iterator();
		while(it.hasNext()) {
			Object obj = it.next();
			if(obj instanceof ResultSet)
				((ResultSet)obj).close();
			else if(obj instanceof Statement)
				((Statement)obj).close();
			else if(obj instanceof PreparedStatement)
				((PreparedStatement)obj).close();
			obj = null;
		}			
		
		openObjects.clear();
	}	
	
	@Override
	protected void finalize() throws Throwable {

		if(con!=null)
			logger.warn("CHeck resource usage: transaction #" + n + 
					" was automatically purged.");
		super.finalize();
	}

}
/* ****************************************************************************************************************** */
