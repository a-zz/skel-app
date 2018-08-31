/* ****************************************************************************************************************** *
 * TextFileMonitor.java                                                                                               *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import io.github.azz.logging.AppLogger;

/**
 * UNIX's "tail -f" style text file monitor: keeps track of text files for newly appended lines. Usage intended 
 * 	(although not restricted to): tracking OS daemons via their log files.
 * @author a-zz
 * TODO Production-grade testing needed!
 */
public class TextFileMonitor implements Runnable {

	private static AppLogger logger = new AppLogger(TextFileMonitor.class);
	
	private final int DEFAULT_POLL_INTERVAL = 60000;
	
	private HashMap<String,File> fileMap;
	private HashMap<File,Integer> pollIntervalMap;
	private HashMap<File,Long> lastPollMap;
	private HashMap<File,Long> lastKnownPositionMap;
	private HashMap<File,Object> targetObjectMap;
	private HashMap<File,Method> targetMethodMap;
	
	private int overallPollInterval = DEFAULT_POLL_INTERVAL;
	private boolean paused = false;
	private Thread monitoringThread;
	
	private TextFileMonitor manager;
		
	/**
	 * Constructor: creates a new file monitor. This object manages data about tracked files; monitoring actually is
	 * 	done in a separate thread that's started whenever the first file to be tracked is added.
	 * @see addFile() method
	 */
	public TextFileMonitor() {
		
		fileMap = new HashMap<>();
		pollIntervalMap = new HashMap<>();
		lastPollMap = new HashMap<>();
		lastKnownPositionMap = new HashMap<>();
		targetObjectMap = new HashMap<>();
		targetMethodMap = new HashMap<>();
		
		// The monitoring thread is created now, but it won't be fired until the first file is added for tracking. Not  
		//	only because the monitor is useless while there're no files to track; the main reason is that the default 
		//	poll interval for the monitor is 60 seconds, and it's only lowered when a file requires a shorter one (the 
		//	monitor poll interval is set to the shortest one for all files). Starting the monitor while there're no 
		//	files to track means that the first files won't be polled for 60 seconds, which may be inappropiate for
		//	certain situations and misleading in general.
		monitoringThread = new Thread(new TextFileMonitor(this));
	}

	/**
	 * Adds a new file to be tracked. New text lines appended to the file are sent to a target 
	 * 	object.method(String, String).
	 * 	The invocation of this method is done asynchronously (in a separate Thread), to avoid a slow receiving method
	 * 	to block the polling sequence for all files. The FeedbackAndForget class is used for this. The invocation
	 * 	thread is given a timeout equal to the polling interval for the file, to avoid thread count buildup.
	 * @param fileToTrack (File) The file to be tracked. It doesn't need to exist right now; it'll be tested 
	 * 	periodically anyway. 
	 * @param pollInterval (int) The polling interval for the new file. 
	 * @param targetObject (Object) The target object for new lines. null if the target method is static.
	 * @param targetMethod (Method) The target method for new lines. It should have two String arguments:<ul>
	 * 	<li>The tracked file ID (as returned by this method).</li>
	 * 	<li>The text line newly appended to the file.</li>
	 * </ul>
	 * @return (String) A unique ID for the added file.
	 */
	public synchronized String addFile(File fileToTrack, int pollInterval, Object targetObject, Method targetMethod) {
		
		String id = UUID.randomUUID().toString();
		
		fileMap.put(id, fileToTrack);
		pollIntervalMap.put(fileToTrack, new Integer(pollInterval));
		lastPollMap.put(fileToTrack, new Long(0));
		if(fileToTrack.exists())
			lastKnownPositionMap.put(fileToTrack, new Long(fileToTrack.length()));
		else {
			logger.warn("Tracked file " + fileToTrack + " doesn't exist yet");
			lastKnownPositionMap.put(fileToTrack, new Long(0));
		}
		targetObjectMap.put(fileToTrack, targetObject);
		targetMethodMap.put(fileToTrack, targetMethod);
		
		logger.debug("Adding new file to track: " + fileToTrack);
		
		if(fileMap.size()==1) {
			monitoringThread.start();
			logger.trace("Monitor thread starting now");
		}		
		
		return id;
	}
	
	/**
	 * Stops a file from tracking and forget about it forever.
	 * @param id (String) The file unique ID.
	 */
	public synchronized void removeFile(String id) {
		
		File trackedFile = fileMap.get(id);
		pollIntervalMap.remove(trackedFile);		
		lastPollMap.remove(trackedFile);
		lastKnownPositionMap.remove(trackedFile);
		targetObjectMap.remove(trackedFile);
		targetMethodMap.remove(trackedFile);
		logger.debug("File not tracked anymore: " + trackedFile);
	}
	
	/**
	 * Pauses the polling of files. Lines added meanwhile will be retrieved after resuming.
	 */
	public void pause() {
		
		this.paused = true;
	}
	
	/**
	 * Resumes the polling of files.
	 */
	public void resume() {
		
		this.paused = false;
	}
	
	/**
	 * Stops tracking all files and kill the monitoring thread. As per the overridden Object.finalize() contract, this 
	 * 	method will be run on the disposal of the object by the garbage collector; nevertheless, it's avisable to call 
	 * 	it explicitly in order to avoid any running overhead when the daemons are no longer required.
	 */
	@Override
	public void finalize() {
		
		for(String id : fileMap.keySet())
			removeFile(id);
		monitoringThread.interrupt();
		logger.debug("File monitor finalized");
	}
	
	/* * Code below implementing the monitoring thread ************************************************************** */
	
	private TextFileMonitor(TextFileMonitor manager) {
		
		this.manager = manager;
	}

	@Override
	public void run() {
		
		try {
			while(true) {
				long now = new Date().getTime();
				
				if(manager.paused)
					continue;
				
				// If no files are being tracked, poll interval is set to the default (although it's set at object 
				//	construction, must be re-checked periodically just in case every every file previously tracked is
				//	removed).  
				if(manager.fileMap.size()==0)
					overallPollInterval = DEFAULT_POLL_INTERVAL;
				
				for(String id : manager.fileMap.keySet()) {
					File trackedFile = manager.fileMap.get(id);
					
					// Overall polling interval is set to the minimum specified by daemons
					overallPollInterval = overallPollInterval>manager.pollIntervalMap.get(trackedFile)?
							manager.pollIntervalMap.get(trackedFile):
								overallPollInterval;
					try {
						long lastPoll = manager.lastPollMap.get(trackedFile).longValue();
						int pollInterval = manager.pollIntervalMap.get(trackedFile);
						if(now < lastPoll + pollInterval)
							continue;
						
						long lastKnownPosition = manager.lastKnownPositionMap.get(trackedFile).longValue(); 						
						if(lastKnownPosition>trackedFile.length()) {
							lastKnownPosition = trackedFile.length();
							logger.trace("Tracked file id " + id + " was resetted.");
						}
						RandomAccessFile raf = new RandomAccessFile(trackedFile, "r");
						raf.seek(lastKnownPosition);
						String line;						
						while((line=raf.readLine())!=null) {
							logger.trace("Got new line for traked file id " + id + ": " + line);
							FeedbackAndForget.send(manager.targetObjectMap.get(trackedFile), 
									manager.targetMethodMap.get(trackedFile), id, line,
									manager.pollIntervalMap.get(trackedFile).longValue());
						}
						manager.lastKnownPositionMap.put(trackedFile, raf.getFilePointer());
						manager.lastPollMap.put(trackedFile, new Long(now));
						raf.close();
					}
					catch(FileNotFoundException e) {
						logger.warn("Tracked file missing!: " + trackedFile);
					}
					catch(IOException e) {
						logger.warn("Error reading " + trackedFile + ": " + e.getMessage());
					}
				}
				
				Thread.sleep(overallPollInterval);
			}		
		}
		catch(InterruptedException e) {
			logger.warn("File monitor thread was interrupted!");
		}
	}
}
/* ****************************************************************************************************************** */

