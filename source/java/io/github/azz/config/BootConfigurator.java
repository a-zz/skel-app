/* ****************************************************************************************************************** *
 * BootConfigurator.java                                                                                              *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.github.azz.logging.AppLogManager;
import io.github.azz.logging.AppLogger;
import io.github.azz.sql.DbManager;
import io.github.azz.sql.rdbms.RdbmsSupport;
import io.github.azz.util.Scheduler;

/**
 * Performs initial configuration at startup. So far:
 * <ol>
 * <li>Initialize the logging utility (from WEB-INF/log4j2.xml file)</li>
 * <li>Initialize the local configuration container (from WEB-INF/local.properties file)</li>
 * <li>Initialize the database management facility</li>
 * </ol>
 * Execution is launched at boot as a web application listener, as defined in WEB-INF/web.xml
 * @author a-zz
 */
public class BootConfigurator  implements ServletContextListener {

	private static final String log4j2ConfigPath = "WEB-INF/log4j2.xml";
	private static final String localPropertiesPath = "WEB-INF/local.properties";
	
	public void contextInitialized(ServletContextEvent sce) {
		
		// 1. Initialize the logging utility
		AppLogger logger;
		try {
			AppLogManager.initialize(sce.getServletContext().getRealPath(log4j2ConfigPath));
			logger = new AppLogger(BootConfigurator.class);
		}
		catch(Exception e) {
			throw new RuntimeException("Unable to initialize logging utility: " + e.getMessage());
		}
		
		// 2. Initialize the local configuration container
		try {
			LocalConfiguration.initialize(sce.getServletContext().getRealPath(localPropertiesPath));
		}
		catch(IOException e) {
			String message = "Can't read local properties file: " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException(message);
		}

		// 3. Initialize the database management facility
		try {
			DbManager.initialize();
		}
		catch(Exception e) {
			String message = "Unable to initialize the database management facility: " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException(message);
		}

		// 4. Check wether we're in production or test mode (if app property "app.production" is set, whichever its
		//	value, we're in production mode)
		boolean testMode = true; 
		try {
			testMode = (AppConfiguration.getProperty("app.production")==null);
		}
		catch(SQLException e) {
			String message = "Unable to read application configuration properties: " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException();
		}
		
		// 5. Check wether database support is complete
		try {
			RdbmsSupport.checkImplementation(DbManager.getDatabaseEngine(), !testMode);
		}
		catch(IOException e) {
			String message = "Unable to check database engine support (weird!): " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException();
		}
		catch(ClassNotFoundException e) {
			String message = "Unable to check database engine support (weird!): " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException();
		}
		catch(UnsupportedOperationException e) {
			logger.fatal(e.getMessage());
			throw new RuntimeException();
		}
		
		// 6. Initialize the scheduler
		try {
			Scheduler.initialize();
		}
		catch(Exception e) {
			logger.fatal("Unable to initialize task scheduler: " + e.getMessage());
			throw new RuntimeException();
		}
		
		// All done
		logger.info("\\o/ --> " + sce.getServletContext().getServletContextName() + " up and running! <-- \\o/");
		if(testMode) 
			logger.warn("Currently running in ***TEST*** mode!!! (set application property app.production to any " +
					"value to change into production mode)");
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
		// Shutdown task scheduler
		Scheduler.shutdown();
		
		AppLogger logger = new AppLogger(BootConfigurator.class);
		logger.info(":_( --> " + sce.getServletContext().getServletContextName() + " shut down! <-- )_:");
	}
}

/* ****************************************************************************************************************** */