/* ****************************************************************************************************************** *
 * AppLogManager.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Application logging manager utility
 * @author a-zz
 */
public class AppLogManager {

	/**
	 * Initializes the logging utility from a log4j2 configuration file.
	 * @param configFilePath (String) Path to the configuration file
	 * @param greeting (String) The first message to be logged (with INFO level). Be creative! (null is ok, though)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void initialize(String configFilePath, String greeting) throws FileNotFoundException, IOException {
		
		ConfigurationSource source = new ConfigurationSource(new FileInputStream(configFilePath));
		Configurator.initialize(null, source);
		AppLogger logger = new AppLogger(AppLogManager.class);
		if(greeting!=null)
			logger.info(greeting);
		logger.debug("Logging utility initialized!");
	}
}
/* ****************************************************************************************************************** */
