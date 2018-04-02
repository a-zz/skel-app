/* ****************************************************************************************************************** *
 * RdbmsSupport.java                                                                                                  *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */
package io.github.azz.sql.rdbms;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import io.github.azz.logging.AppLogger;
import io.github.azz.sql.DaInterface;
import io.github.azz.sql.SqlTransaction;
import io.github.azz.util.Reflection;

/**
 * Utility class for checking RDBMS engine support implementation. Please read doc/dbsupport.txt for extended info.
 * @author a-zz
 */
public class RdbmsSupport {

	private static AppLogger logger = new AppLogger(RdbmsSupport.class);
	
	/**
	 * Supported database engines
	 */
	public enum EnumDatabaseEngines { HSQLDB };
	
	/**
	 * Checks wether current database engine is fully supported by application code. This is as simple as checking that
	 * 	every interface extending DaInterface is implemented by a class also implementing the corresponding interface 
	 * 	for a database engine.
	 * 	<br/><br/>
	 * 	E.g. if current database engine is HSQLDB and we're checking MyClassDaInterface:
	 * 	<br/><br/>
	 * MyClassHSQLDB <- MyClassDaInterface <- DaInterface: is ok as long as MyClassHSQLDB implements both 
	 * 	MyClassDaInterface and HsqldbInterface.
	 * @param databaseEngine (enumDatabaseEngines) The engine to be checked
	 * @param enforce (boolean) If set to false, incomplete implementation would only log a warning message; if true,
	 *  would raise an...
	 * @throws UnsupportedOperationException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void checkImplementation(EnumDatabaseEngines databaseEngine, boolean enforce) 
			throws UnsupportedOperationException, IOException, ClassNotFoundException {
		
		// 1. Get interface for running engine
		Class engineInterface = getEngineInterface(databaseEngine);
		
		// 2. Find classes extending DaInterface
		String packageName = RdbmsSupport.class.getPackage().getName();
		String basePackage = packageName.substring(0, packageName.lastIndexOf("."));
		basePackage = basePackage.substring(0, basePackage.lastIndexOf("."));		
		ArrayList<String> daInterfacesList = new ArrayList<String>();
		Reflection.scanPackage(basePackage, true, daInterfacesList, DaInterface.class.getName(), null);
		
		// 3. Check whether every interface found before has an implementing class also implementing the selected engine 
		//	interface. Implementations are to be found in the same package.
		String implMissing = "";
		for(String className : daInterfacesList) {
			Class classFound = Class.forName(className);
			if(classFound.isInterface()) {
				ArrayList<String> implClassesInPackage = new ArrayList<String>();
				String classFoundPackageName = classFound.getPackage().getName();
				Reflection.scanPackage(classFoundPackageName, false, implClassesInPackage, 
						engineInterface.getName(), null);
				boolean missing = true;
				for(String implClass : implClassesInPackage) {
					if(classFound.isAssignableFrom(Class.forName(implClass))) {
						missing = false;
						break;
					}
				}
				if(missing)
					implMissing += className + " ";
			}
		}
		
		// 4. All done
		if(!implMissing.equals("")) {
			String message = "Missing " + databaseEngine + " implementation for data access interfaces: " + implMissing;
			if(enforce)
				throw new UnsupportedOperationException(message);
			else
				logger.warn(message);
		}
		else
			logger.debug("Checked database engine support for " + databaseEngine + ": fully supported!");
	}
	
	/**
	 * Register the database driver class for the database URL provided. This method must be modified when adding 
	 * 	support for new database engines.
	 * @param url (String) The database URL provided.
	 * @return (enumDatabaseEngines) The database engine selected. 
	 * @throws ClassNotFoundException
	 * @throws UnsupportedOperationException
	 */
	public static EnumDatabaseEngines registerDriver(String url) 
			throws ClassNotFoundException, UnsupportedOperationException {
		
		if(url.startsWith("jdbc:hsqldb")) {
			Class.forName("org.hsqldb.jdbcDriver");
			logger.debug("HSQLDB JDBC driver loaded");
			return EnumDatabaseEngines.HSQLDB;
		}
		else
			throw new UnsupportedOperationException("Database support not implemented for URL: " + url);
	}		
	
	/**
	 * Returns the interface for a database engine
	 * @param databaseEngine (enumDatabaseEngines) 
	 * @return (Class)
	 */
	@SuppressWarnings("rawtypes")
	public static Class getEngineInterface(EnumDatabaseEngines databaseEngine) {
		
		String packageName = RdbmsSupport.class.getPackage().getName();
		String interfaceName = packageName + "." + databaseEngine + "Interface";
		Class engineInterface = null;
		
		try {
			engineInterface = Class.forName(interfaceName);
		}
		catch(ClassNotFoundException e) {
			throw new UnsupportedOperationException("Database engine interface " + interfaceName + " not found: "
					+ e.getMessage());
		}
		
		if(!engineInterface.isInterface())
			throw new UnsupportedOperationException("Can't get engine interface: " + interfaceName + 
					" is not an inteface");
		else
			return engineInterface;
	}
	
	/**
	 * Check a transaction isolation level support for the current database engine. 
	 * @param isolationLevel (SqlTransaction.EnumIsolationLevels)
	 * @throws SQLException if the level choosen is definitely not supported by the engine. Otherwise it returns
	 * 	quietly, maybe logging some message.
	 */
	public static void checkIsolationLevelSupport(EnumDatabaseEngines databaseEngine,
			SqlTransaction.EnumIsolationLevels isolationLevel) throws SQLException {
		
		switch(databaseEngine) {
		case HSQLDB:
			switch(isolationLevel) {
			case READ_UNCOMMITTED:
				logger.debug("In HSQLDB v2.x or higher, transaction level READ UNCOMMITED " +
						"is upgraded to READ COMMITED");
				break;
			case REPEATABLE_READ:
				logger.debug("In HSQLDB v2.x or higher, transaction level REPEATABLE READ " +
						"is upgraded to SERIALIZABLE");
				break;
			default:
				// All fine, nothing to say
			}
		}
	}
}
/* ****************************************************************************************************************** */