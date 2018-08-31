/* ****************************************************************************************************************** *
 * DaemonMonitor.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.github.azz.logging.AppLogger;
import io.github.azz.util.DaemonManager.OutputLine;

/**
 * Monitor and output (stdout & stderr) poller for daemons. Intended to be used from DaemonManager, rather than 
 * 	independently. 
 * @author a-zz
 */
public class DaemonMonitor implements Runnable {

	private static AppLogger logger = new AppLogger(DaemonMonitor.class);
	
	private DaemonManager manager;
	
	private final int DEFAULT_POLL_INTERVAL = 60000;
	private int pollInterval = DEFAULT_POLL_INTERVAL;	
	private HashMap<Daemon,Long> lastPollMap;

	/**
	 * Constructor: creates a daemon monitor.
	 * @param manager (DaemonManager) The related daemon manager.
	 */
	public DaemonMonitor(DaemonManager manager) {
		
		this.manager = manager;
		this.lastPollMap = new HashMap<>();
	}

	@Override
	public void run() {
		
		try {
			while(true) {
				long now = new Date().getTime();
				
				// If no daemons are running, poll interval is set to the default (although it's set at object 
				//	construction, must be re-checked periodically just in case every running daemons are stopped).  
				if(manager.getDaemons().size()==0)
					pollInterval = DEFAULT_POLL_INTERVAL;
				
				for(String id : manager.getDaemons()) {
					Daemon daemon = manager.getDaemon(id);
					
					// Overall polling interval is set to the minimum specified by daemons 
					if(pollInterval>manager.getPollInterval(daemon)) {
						pollInterval = manager.getPollInterval(daemon);
						logger.trace("Overall poll interval set to " + pollInterval);
					}
						
					// Poll stdout and stderr, given polling interval for daemon has elapsed
					Long lastPoll = lastPollMap.get(daemon);
					if(lastPoll==null || now >= lastPoll.longValue() + manager.getPollInterval(daemon)) {
						try {							
							BufferedReader stdout = daemon.getStdout();
							while(stdout.ready()) {
								String stdoutLine = stdout.ready()?stdout.readLine():null;
								if(stdoutLine!=null) {
									logger.trace("Got stdout for daemon id " + id + ": " + stdoutLine);
									ArrayList<OutputLine> stdoutHistory = manager.getStdoutHistory(daemon);
									if(stdoutHistory!=null) 
										stdoutHistory.add(manager.new OutputLine(stdoutLine));
									Method stdoutTargetMethod = manager.getStdoutTargetMethod(id);
									try {
										if(stdoutTargetMethod!=null)
											FeedbackAndForget.send(manager.getStdoutTargetObject(id), 
													stdoutTargetMethod, id, stdoutLine,
													manager.getPollInterval(daemon));
									}
									catch(Exception e) {
										logger.error("Couldn't invoke stdout target method for daemon id " + id + ": " + 
												e.getMessage());
									}
								}
							}
						}
						catch(IOException e) {
							logger.error("Couldn't read stdout for daemon id " + id + ": " + e.getMessage());
						}
						
						try {
							BufferedReader stderr = daemon.getStderr();
							while(stderr.ready()) {
								String stderrLine = stderr.ready()?stderr.readLine():null; 
								if(stderrLine!=null) {
									logger.trace("Got stderr for daemon id " + id + ": " + stderrLine);
									ArrayList<OutputLine> stderrHistory = manager.getStderrHistory(daemon);
									if(stderrHistory!=null)
										stderrHistory.add(manager.new OutputLine(stderrLine));
									Method stderrTargetMethod = manager.getStderrTargetMethod(id);
									try {
										if(stderrTargetMethod!=null)
											FeedbackAndForget.send(manager.getStderrTargetObject(id), 
													stderrTargetMethod, id, stderrLine,
													manager.getPollInterval(daemon));
									}
									catch(Exception e) {
										logger.error("Couldn't invoke stdout target method for daemon id " + id + ": " + 
												e.getMessage());
									}
								}
							}
						}
						catch(IOException e) {
							logger.error("Couldn't read stderr for daemon id " + id + ": " + e.getMessage());
						}			
						lastPollMap.put(daemon, now);
					}					
				}
				Thread.sleep(pollInterval);
			}
		}
		catch(InterruptedException e) {
			logger.debug("Daemon monitor thread was interrupted");
		}
	}	
}
/* ****************************************************************************************************************** */

