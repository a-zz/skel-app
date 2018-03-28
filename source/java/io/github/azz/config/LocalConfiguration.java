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
		
		AppLogger logger = new AppLogger(LocalConfiguration.class);
		
		try {
			if(propertiesFile.lastModified()>propertiesFileTimeStamp) {			
				p.load(new FileInputStream(propertiesFile));
				propertiesFileTimeStamp = propertiesFile.lastModified();				
				logger.debug("Local configuration reloaded (file modified on disk)");
			}
		}
		catch(NullPointerException e) {
			logger.error("Local configuration not initialized");
			throw e;
		}
	}

	/**
	 * Compares the key set in a properties file against other.
	 * <br/><br/>
	 * This is useful at build time to check whether new releases have changed the WEB-INF/local.properties and the
	 * 	local copy kept at config/ should be updated.   
	 * @param fileToBeChecked (File) The properties file to be checked
	 * @param fileComparedTo (File) The properties file used for comparison
	 * @return (String) A list of missing properties in the checked file, empty string if none.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String checkProperties(File fileToBeChecked, File fileComparedTo) 
			throws FileNotFoundException, IOException {
		
		Properties toBeChecked = new Properties();
		toBeChecked.load(new FileInputStream(fileToBeChecked));
		Properties comparedTo = new Properties();
		comparedTo.load(new FileInputStream(fileComparedTo));
		
		String missingKeys = "";
		for(Object keyToBeChecked : comparedTo.keySet()) {
			if(toBeChecked.getProperty(keyToBeChecked.toString())==null)
				missingKeys += keyToBeChecked.toString() + " ";
		}
		
		return missingKeys;
	}
		
	/**
	 * Runs the checkLocalCopy() method
	 * @param args (String[]) Two property files, by name:
	 * 	[0] The file to be checked
	 * 	[1] The file to be compared to
	 * @throws Exception
	 * @see checkProperties()
	 */
	public static void main(String args[]) throws Exception {
		
		System.out.println(checkProperties(new File(args[0]), new File(args[1])));
	}
}
/* ****************************************************************************************************************** */