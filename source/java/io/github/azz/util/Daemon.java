/* ****************************************************************************************************************** *
 * Daemon.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.azz.logging.AppLogger;

/**
 * A simple class for running daemons (OS background processes) as a separate thread. Intended to be used from 
 * 	DaemonManager, rather than independently.
 * @author a-zz
 */
public class Daemon implements Runnable {

	private static AppLogger logger = new AppLogger(Daemon.class);
	
	private String id;
	private Process process;
	
	/**
	 * Constructor: creates a new Daemon. 
	 * @param id (String) The daemon's unique ID, for logging purposes.
	 * @param cmdLine (String) The daemon command line
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Daemon(String id, String cmdLine) throws IOException, SecurityException {
		
		this.id = id;
		this.process = Runtime.getRuntime().exec(cmdLine);
	}
		
	@Override
	public void run() {
		
		try {
			logger.warn("Daemon " + id + " ended with code " + process.waitFor());
		}
		catch(InterruptedException e) {
			logger.warn("Daemon " + id + " was interrupted: " + e.getMessage());
		}		
	}
	
	/**
	 * Gets the daemon stdout.
	 * @return (BufferedReader)
	 */
	public BufferedReader getStdout() {
		
		return new BufferedReader(new InputStreamReader(process.getInputStream()));
	}
	
	/**
	 * Gets the daemon stderr.
	 * @return (BufferedReader)
	 */
	public BufferedReader getStderr() {
		
		return new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}
}
/* ****************************************************************************************************************** */
