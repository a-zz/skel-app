/* ****************************************************************************************************************** *
 * DaInterface.java                                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import io.github.azz.logging.AppLogger;

/**
 * Super-interface for data access interfaces 
 * @author a-zz
 */
public interface DaInterface {

	/**
	 * Gets data access interface for certain class
	 * @param clazz (Class)
	 * @return Class<? extends DaInterface>
	 */
	@SuppressWarnings("rawtypes")
	public static Class<? extends DaInterface> getImplClassFor(Class clazz) {
		
		String dataAccessClassName = null;
		try {
			String className = clazz.getName();			
			dataAccessClassName = className.substring(0, className.lastIndexOf(".")) +
					".da." + 
					className.substring(className.lastIndexOf(".")+1) + 
					DbManager.getDatabaseEngine();  		
			return Class.forName(dataAccessClassName).asSubclass(DaInterface.class);
		}
		catch(ClassNotFoundException e) {
			AppLogger logger = new AppLogger(DbManager.class);
			logger.error("Requested data access class not found: " + dataAccessClassName);
			return null;
		}
	}
}
/* ****************************************************************************************************************** */