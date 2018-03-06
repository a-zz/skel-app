/* ****************************************************************************************************************** *
 * DaInterface.java                                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import io.github.azz.logging.AppLogger;
import io.github.azz.sql.rdbms.RdbmsSupport;

/**
 * Super-interface for data access interfaces 
 * @author a-zz
 */
public interface DaInterface {

	/**
	 * Gets data access interface for certain data-access consumer class and the current database engine.
	 * @param clazz (Class) The data-access consumer class.
	 * @return Class<? extends DaInterface> The data access interface. If several are found, only the first one is
	 * 	returned. 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Class<? extends DaInterface> getImplClassFor(Class clazz) {
		
		AppLogger logger = new AppLogger(DbManager.class);
		
		try {
			Class engineInterface = RdbmsSupport.getEngineInterface(DbManager.getDatabaseEngine());
			String packageName = clazz.getPackage().getName();
			packageName = packageName + ".da";
	        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();;
	        Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));
	        File dataAccessClassDirectory = new File(resources.nextElement().getFile());
	        File[] classFiles =  dataAccessClassDirectory.listFiles();
	        for(int i = 0; i<classFiles.length; i++) {
	        	Class classFound = Class.forName(packageName + "." +
	        			(classFiles[i].getName().substring(0, classFiles[i].getName().length()-6)));
	        	if(DaInterface.class.isAssignableFrom(classFound) &&
	        			engineInterface.isAssignableFrom(classFound))
	        		return classFound;
	        }
		}
		catch(Exception e) {
			logger.error("Data access implementation class not found for " +  clazz.getName() 
					+ " and engine " + DbManager.getDatabaseEngine());
			return null;
		}
		
		logger.error("Data access implementation class not found for " +  clazz.getName() 
				+ " and engine " + DbManager.getDatabaseEngine());
		return null;
	}	
}
/* ****************************************************************************************************************** */