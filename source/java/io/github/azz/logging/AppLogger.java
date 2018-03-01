/* ****************************************************************************************************************** *
 * AppLogManager.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.logging;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Application logging utility
 * @author a-zz
 */
public class AppLogger {

	private String sessionInfo;
	private Logger logger;
	
	/**
	 * Constructor: logger utility for a class
	 * @param clazz (Class) The class
	 */
	public AppLogger(@SuppressWarnings("rawtypes") Class clazz) {
		
		sessionInfo = "INTERNAL";
		logger = LogManager.getLogger(clazz);
	}
	
	/**
	 * Constructor: logger utility for a class with prepended HTTP session information
	 * @param clazz (class) The class
	 * @param httpSession (HttpSession) The HTTO session
	 */
	public AppLogger(@SuppressWarnings("rawtypes") Class clazz, HttpSession httpSession) {
		
		sessionInfo = httpSession.getId().toString();
		logger = LogManager.getLogger(clazz);
	}
	
	/**
	 * Logs a message with TRACE level
	 * @param message (String) The message to log
	 */
	public void trace(String message) {
		
		logger.trace("{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with SQL level. This is a custom level for database operation messages.
	 * @param message (String) The message to log
	 */
	public void sql(String message) {
	
		logger.log(Level.forName("SQL", 550), "{" + sessionInfo + "} " + message);
	}  
	
	/**
	 * Logs a message with DEBUG level
	 * @param message (String) The message to log
	 */
	public void debug(String message) {
		
		logger.debug("{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with INFO level
	 * @param message (String) The message to log
	 */
	public void info(String message) {
		
		logger.info("{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with DPRTCT level. This is a custom level for data-protection related messages, e.g. accessing
	 * 	sensitive data or law-enforced auditing.
	 * @param message (String) The message to log
	 */
	public void dprtct(String message) {
		
		logger.log(Level.forName("SQL", 350), "{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with WARN level
	 * @param message (String) The message to log
	 */
	public void warn(String message) {
		
		logger.warn("{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with ERROR level
	 * @param message (String) The message to log
	 */
	public void error(String message) {
		
		logger.error("{" + sessionInfo + "} " + message);
	}
	
	/**
	 * Logs a message with FATAL level
	 * @param message (String) The message to log
	 */
	public void fatal(String message) {
		
		logger.fatal("{" + sessionInfo + "} " + message);
	}
}
/* ****************************************************************************************************************** */