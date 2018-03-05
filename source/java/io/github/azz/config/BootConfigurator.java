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

/**
 * Performs initial configuration at startup. So far:
 * <ol>
 * <li>Initialize the logging utility (from WEB-INF/log4j2.xml file)</li>
 * <li>Initialize the local configuration container (from WEB-INF/local.properties file)</li>
 * <li>Initialize the database management facility</li>
 * </ol>
 * @author a-zz
 */
public class BootConfigurator  implements ServletContextListener {

	private static final String log4j2ConfigPath = "WEB-INF/log4j2.xml";
	private static final String localPropertiesPath = "WEB-INF/local.properties";
	
	public void contextInitialized(ServletContextEvent sce) {
		
		// Initialize the logging utility
		AppLogger logger;
		try {
			AppLogManager.initialize(sce.getServletContext().getRealPath(log4j2ConfigPath));
			logger = new AppLogger(BootConfigurator.class);
		}
		catch(Exception e) {
			throw new RuntimeException("Unable to initialize logging utility: " + e.getMessage());
		}
		
		// Initialize the local configuration container
		try {
			LocalConfiguration.initialize(sce.getServletContext().getRealPath(localPropertiesPath));
		}
		catch(IOException e) {
			String message = "Can't read local properties file: " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException(message);
		}

		// Initialize the database management facility
		try {
			DbManager.initialize();
		}
		catch(Exception e) {
			String message = "Unable to initialize the database management facility: " + e.getMessage();
			logger.fatal(message);
			throw new RuntimeException(message);
		}

		// Check wether we're in production or test mode (if app property "app.production" is set, whichever its
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
		
		// All done
		logger.info("\\o/ --> " + sce.getServletContext().getServletContextName() + " up and running! <-- \\o/");
		if(testMode) 
			logger.warn("Currently running in ***TEST*** mode!!! (set application property app.production to any " +
					"value to change into production mode)");
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
		// Nothing to do here, just logging the app stop
		AppLogger logger = new AppLogger(BootConfigurator.class);
		logger.info(":_( --> " + sce.getServletContext().getServletContextName() + " shut down! <-- )_:");
	}
}

/* ****************************************************************************************************************** */