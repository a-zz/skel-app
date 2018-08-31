/* ****************************************************************************************************************** *
 * DaemonManager.java                                                                                                 *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import io.github.azz.logging.AppLogger;

/**
 * Management class for daemons (background OS processes). 
 * <br/><br/>
 * Whenever a DaemonManager is created, a DaemonMonitor object is run as a separate thread for output (stdout & stderr) 
 * 	polling purposes. This monitor polls the running daemons at a fixed interval. Data polled by the monitor can be 
 * 	kept as a history record, sent to an object.method(String) or both.
 * <br/><br/> 
 * When keeping output history, please consider that in fact there're four time intervals involved:<ol>
 * 	<li>The interval at which the underlying process outputs data. This may be uneven and unpredictable.</li>
 * 	<li>The polling interval for the daemon.</li>
 * 	<li>The overall polling interval for the monitor. It is automagically set up to the lowest for the monitored 
 * 		daemons, but every individual daemon will be polled only after its individual interval is elapsed.</li>
 * 	<li>The polling interval of the classes consuming the history kept.</li>     
 * </ol>Intervals 2 and 4 should be carefully tweaked in order to achieve the best results (avoiding abusive -and
 * 	possibly unneed- polling while keeping up to the pace that's functionally required).
 * <br/><br/>
 * When sending output data to an object.method(), invoking of the method is done asynchronously (in a separate Thread)
 * 	to avoid a slow remote method blocking the monitor poll loop. The FeedbackAndForget class is used for this. The 
 * 	invocation thread is given a timeout equal to the polling interval for the daemon, to avoid thread count buildup.
 * <br/><br/>
 * It must be noted that proper OS daemons aren't considered; if needed to, their log files may be tracked via the 
 * 	TextFileMonitor class.
 * <br/><br/>
 * Also to be noticed: daemons are meant to run "forever", so this class may be inappropiate for running OS commands 
 * 	that perform a task and exit. Of course it may be used for that, but beware that the command exit code, which is 
 * 	usually needed to get feedback from OS commands, is just logged, not returned. The ServerTask class should be more 
 * 	convenient in these situations.
 * @see TextFileMonitor 
 * @see ServerTask
 * @author a-zz
 * TODO Production-grade testing needed!
 */
public class DaemonManager {

	private static AppLogger logger = new AppLogger(DaemonManager.class); 		
	
	private HashMap<String,Daemon> daemonMap;	
	private HashMap<Daemon,Integer> pollIntervalMap;
	private HashMap<Daemon,Thread> threadMap;
	private HashMap<Daemon,ArrayList<OutputLine>> stdoutHistoryMap;
	private HashMap<Daemon,ArrayList<OutputLine>> stderrHistoryMap;
	private HashMap<Daemon,Object> stdoutTargetObjectMap;
	private HashMap<Daemon,Object> stderrTargetObjectMap;
	private HashMap<Daemon,Method> stdoutTargetMethodMap;
	private HashMap<Daemon,Method> stderrTargetMethodMap;	
	
	private Thread monitorThread;
	
	/**
	 * Constructor: creates a daemon manager (and its related DaemonMonitor thread)
	 */
	public DaemonManager() {
	
		// Populating the object 
		daemonMap = new HashMap<>();
		pollIntervalMap = new HashMap<>();
		threadMap = new HashMap<>();
		stdoutHistoryMap = new HashMap<>();
		stderrHistoryMap = new HashMap<>();
		stdoutTargetObjectMap = new HashMap<>();
		stderrTargetObjectMap = new HashMap<>();
		stdoutTargetMethodMap = new HashMap<>();
		stderrTargetMethodMap = new HashMap<>();		
		
		// The monitor thread is created now, but it won't be fired until the first daemon is started up. Not only 
		//	because the monitor is useless while there're no daemons to poll; the main reason is that the default poll
		//	interval for the monitor is 60 seconds, and it's only lowered when a daemon requires a shorter one (the 
		//	monitor poll interval is set to the shortest one for all daemons). Starting the monitor while there're no 
		//	daemons running means that the first daemon won't be polled for 60 seconds, which may be inappropiate for
		//	certain situations and misleading in general.
		DaemonMonitor monitor = new DaemonMonitor(this);
		monitorThread = new Thread(monitor);		
		logger.trace("Daemon monitor thread is " + monitorThread + " (start deferred up to first daemon startup)"); 
		
		// Al done!
		logger.debug("Daemon manager succesfully started");		
	}

	/**
	 * Creates a new daemon, but doesn't start it, startDaemon() should be called afterwards (allowing for prior
	 * 	configuration; particularly setStdoutTarget() and setStderrTarget(), if needed). 
	 * @param cmdLine (String) The daemon command line.
	 * @param pollInterval (int) Polling interval for the daemon's stdout and stderr.
	 * @param keepStdoutHistory (boolean) Sets whether stdout history should be kept.
	 * @param keepStderrHistory (boolean) Sets whether stderr history should be kept.
	 * @return (String) A unique ID assigned to the daemon.
	 */
	public synchronized String createDaemon(String cmdLine, int pollInterval, 
			boolean keepStdoutHistory, boolean keepStderrHistory) {
		
		String id = UUID.randomUUID().toString();
		Daemon daemon = null;
		
		try {			
			daemon = new Daemon(id, cmdLine);
			pollIntervalMap.put(daemon, new Integer(pollInterval));
			if(keepStdoutHistory)
				stdoutHistoryMap.put(daemon, new ArrayList<>());
			if(keepStderrHistory)
				stderrHistoryMap.put(daemon, new ArrayList<>());
			Thread thread = new Thread(daemon);
			thread.setDaemon(true);
			threadMap.put(daemon, thread);			
			daemonMap.put(id, daemon);
			if(daemonMap.size()==1) {
				monitorThread.start();
				logger.trace("Monitor thread starting now");
			}
			logger.debug("New daemon created with id " + id);
			return id;
		}
		catch(Exception e) {
			if(daemon!=null) {
				pollIntervalMap.remove(daemon);
				threadMap.remove(daemon);
				if(keepStdoutHistory)
					stdoutHistoryMap.remove(daemon);
				if(keepStderrHistory)
					stderrHistoryMap.remove(daemon);
			}
			logger.error("Couldn't start daemon id " + id + ": " + e.getMessage());
			return null;
		}	
	}
	
	/**
	 * Sets a target object.method() for the daemon's polled stdout lines.
	 * @param id (String) The daemon unique ID.
	 * @param targetObject (Object) The target object. May be null if target method is static.
	 * @param targetMethod (Method) The target method. Should have an only String argument. 
	 */
	public void setStdoutTarget(String id, Object targetObject, Method targetMethod) {
		
		Daemon daemon = daemonMap.get(id);
		stdoutTargetObjectMap.put(daemon, targetObject);
		stdoutTargetMethodMap.put(daemon, targetMethod);
	}
	
	/**
	 * Gets the target object for the daemon's stdout. 
	 * @param id (String) The daemon unique ID.
	 * @return (Object) The target object. It may be null when the target method is static, so don't use this method
	 * 	to know whether stoudt should be sent to an object.method(); use getStdoutTargetMethod() instead.
	 */
	public Object getStdoutTargetObject(String id) {
		
		Daemon daemon = daemonMap.get(id);
		return stdoutTargetObjectMap.get(daemon);
	}

	/**
	 * Gets the target method for the daemon's stdout.
	 * @param id (String) The daemon unique ID.
	 * @return (Method) The target method.
	 */
	public Method getStdoutTargetMethod(String id) {
		
		Daemon daemon = daemonMap.get(id);
		return stdoutTargetMethodMap.get(daemon);
	}	
	
	/**
	 * Sets a target object.method() for the daemon's polled stderr lines.
	 * @param id (String) The daemon unique ID.
	 * @param targetObject (Object) The target object. May be null if target method is static.
	 * @param targetMethod (Method) The target method. Should have an only String argument. 
	 */
	public void setStderrTarget(String id, Object targetObject, Method targetMethod) {
		
		Daemon daemon = daemonMap.get(id);
		stderrTargetObjectMap.put(daemon, targetObject);
		stderrTargetMethodMap.put(daemon, targetMethod);
	}	
	
	/**
	 * Gets the target object for the daemon's stderr. 
	 * @param id (String) The daemon unique ID.
	 * @return (Object) The target object. It may be null when the target method is static, so don't use this method
	 * 	to know whether stderr should be sent to an object.method(); use getStderrTargetMethod() instead.
	 */
	public Object getStderrTargetObject(String id) {
		
		Daemon daemon = daemonMap.get(id);
		return stdoutTargetObjectMap.get(daemon);
	}

	/**
	 * Gets the target method for the daemon's stderr.
	 * @param id (String) The daemon unique ID.
	 * @return (Method) The target method.
	 */
	public Method getStderrTargetMethod(String id) {
		
		Daemon daemon = daemonMap.get(id);
		return stdoutTargetMethodMap.get(daemon);
	}		
	
	/**
	 * Starts a daemon previously created and configured.
	 * @param id (String) The daemon unique ID.
	 */
	public void startDaemon(String id) {
		
		threadMap.get(daemonMap.get(id)).start();
		logger.debug("Daemon id " + id + " started now!");
	}
			
	/**
	 * Stops a daemon
	 * @param id (String) The daemon unique ID
	 */
	public synchronized void stopDaemon(String id) {
		
		Daemon daemon = daemonMap.get(id);
		threadMap.get(daemon).interrupt();
		threadMap.remove(daemon);
		daemonMap.remove(daemon);
		pollIntervalMap.remove(daemon);
		stdoutHistoryMap.remove(daemon);
		stderrHistoryMap.remove(daemon);
		logger.debug("Daemon stopped, id " + id);
	}
	
	/**
	 * Stops all daemons and the monitor thread. As per the overridden Object.finalize() contract, this method will be
	 * 	run on the disposal of the object by the garbage collector; nevertheless, it's avisable to call it explicitly
	 * 	in an orderly application exit (otherwise the monitor thread would prevent the application to stop).
	 */
	@Override
	public void finalize() {
		
		for(String id : daemonMap.keySet())
			stopDaemon(id);
		monitorThread.interrupt();
		logger.debug("Daemon manager finalized");
	}

	/**
	 * Get all the running daemons
	 * @return (Set<String>) A set with the daemons' unique IDs
	 */
	public Set<String> getDaemons() {
		
		return daemonMap.keySet();
	}
	
	/**
	 * Gets a daemon by its ID
	 * @param id (String)
	 * @return (Daemon)
	 */
	public Daemon getDaemon(String id) {
		
		return daemonMap.get(id);
	}
	
	/**
	 * Gets the polling interval for a daemon
	 * @param daemon (Daemon)
	 * @return (int)
	 */
	public int getPollInterval(Daemon daemon) {
		
		return pollIntervalMap.get(daemon).intValue();
	}
		
	/**
	 * Get a line (the oldest one) from a daemon stdout history
	 * @param daemon (Daemon) The daemon to query
	 * @param purge (boolean) Sets whether the line read must be removed
	 * @return (OutputLine) The output line (timestamp + text) or null if there's non available
	 */
	public OutputLine readStdoutFromHistory(Daemon daemon, boolean purge) {
		
		ArrayList<OutputLine> stdoutHistory = stdoutHistoryMap.get(daemon);
		
		if(stdoutHistory==null) {
			logger.warn("Stdout history was disabled for this daemon");
			return null;
		}
		
		if(stdoutHistory.size()==0)
			return null;
		else {
			OutputLine result = stdoutHistory.get(0);
			if(purge)
				stdoutHistory.remove(0);
			return result;
		}
	}
	
	/**
	 * Gets the stdout history for a daemon
	 * @param daemon (Daemon)
	 * @return (ArrayList<OutputLine>)
	 */
	public ArrayList<OutputLine> getStdoutHistory(Daemon daemon) {
		
		return stdoutHistoryMap.get(daemon);
	}
	
	/**
	 * Get a line (the oldest one) from a daemon stderr history
	 * @param daemon (Daemon) The daemon to query
	 * @param purge (boolean) Sets whether the line read must be removed
	 * @return (OutputLine) The output line (timestamp + text) or null if there's non available
	 */
	public OutputLine readStderrFromHistory(Daemon daemon, boolean purge) {
		
		ArrayList<OutputLine> stderrHistory = stderrHistoryMap.get(daemon);
		
		if(stderrHistory==null) {
			logger.warn("Stderr history was disabled for this daemon");
			return null;
		}
		
		if(stderrHistory.size()==0)
			return null;
		else {
			OutputLine result = stderrHistory.get(0);
			if(purge)
				stderrHistory.remove(0);
			return result;
		}
	}	
	
	/**
	 * Gets the stderr history for a daemon
	 * @param daemon (Daemon)
	 * @return (ArrayList<OutputLine>)
	 */	
	public ArrayList<OutputLine> getStderrHistory(Daemon daemon) {
		
		return stderrHistoryMap.get(daemon);
	}	
	
	/**
	 * A subclass for gathering stdout/stderr text lines and their corresponding (polling) timestamp. 
	 * @author a-zz
	 */
	public class OutputLine {
		
		private long timestamp;
		private String line;

		/**
		 * Constructor: creates an OutputLine.
		 * @param line (String) Text line.
		 */
		public OutputLine(String line) {
			
			this.timestamp = new Date().getTime();
			this.line = line;
		}
		
		/**
		 * Gets the line (polling) timestamp.
		 * @return (long) Time in millis since January 1, 1970, 00:00:00 GMT (as in Date.getTime()).
		 */
		public long getTimestamp() {
			
			return timestamp;
		}
		
		/**
		 * Gets the text line.
		 * @return (String)
		 */
		public String getLine() {
			
			return line;
		}
		
		public String toString() {
			
			return  "[" + new Date(timestamp) + "]\t" + line;
		}
	}
}
/* ****************************************************************************************************************** */