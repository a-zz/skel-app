/* ****************************************************************************************************************** *
 * LocalConfiguration.java                                                                                            *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import io.github.azz.logging.AppLogger;

/**
 * Local configuration container. This is intended to hold configurations specific to the deployment machine rather than
 * 	application related, and specially those needed to boot the application. 
 * The configuration is loaded from a .properties file. This class also keeps track of the file modification date,
 * 	so properties changed at runtime can be hot-replaced on the fly.
 * @author a-zz
 */
public class LocalConfiguration {

	private static File propertiesFile;
	private static long propertiesFileTimeStamp; 
	private static Properties p = new Properties();
	
	/**
	 * Initializes the local configuration utility
	 * @param propertiesFilePath (String) Absolute path to .properties file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void initialize(String propertiesFilePath) throws FileNotFoundException, IOException {
		
		propertiesFile = new File(propertiesFilePath);
		propertiesFileTimeStamp = propertiesFile.lastModified();
		p.load(new FileInputStream(propertiesFile));
		AppLogger logger = new AppLogger(LocalConfiguration.class);
		logger.debug("Local configuration loaded!");
	}
	
	/**
	 * Searches for a property with the specified key and return its value.
	 * @param key (String) The key to look for.
	 * @return (String) The property value; null if it's not foud.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getProperty(String key) throws FileNotFoundException, IOException {
		
		checkTimeStampAndReload();
		return p.getProperty(key);
	}
	
	private static void checkTimeStampAndReload() throws FileNotFoundException, IOException {
		
		if(propertiesFile.lastModified()>propertiesFileTimeStamp) {			
			p.load(new FileInputStream(propertiesFile));
			propertiesFileTimeStamp = propertiesFile.lastModified();
			AppLogger logger = new AppLogger(LocalConfiguration.class);
			logger.debug("Local configuration reloaded (file modified on disk)");

		}
	}
}
/* ****************************************************************************************************************** */