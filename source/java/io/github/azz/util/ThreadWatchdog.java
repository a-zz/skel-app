/* ****************************************************************************************************************** *
 * ThreadWatchdog.java                                                                                                *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.github.azz.logging.AppLogger;

/**
 * A class to manage thread timeouts (and kill them when appropiate). Singleton implementation: just a single instance 
 * 	can run in the JVM (plus an additional thread in charge of watching for client threads).
 * <br/><br/>
 * This class is intended to avoid thread count buildup when there're slow or mutually-locking threads involved. 
 * 	Therefore it's not designed to stop threads exactly at their timeout time, but eventually within 
 * 	DEFAULT_LOOP_INTERVAL milliseconds afterwards (currently 5000).  
 * @author a-zz
 * TODO Production-grade testing needed
 */
public class ThreadWatchdog implements Runnable {

	private static AppLogger logger = new AppLogger(ThreadWatchdog.class);
	
	private int DEFAULT_LOOP_INTERVAL = 5000;
	
	private static ThreadWatchdog INSTANCE = null;
	private HashMap<Thread,Long> threadExpirationMap;
	private static Thread workerThread;
	
	private ThreadWatchdog parent;
	
	/**
	 * Gets the (singleton) thread watchdog instance. It's created if it doesn't previously exist.
	 * @return (ThreadWatchdog)
	 */
	public synchronized static ThreadWatchdog getInstance() {
		
		if(INSTANCE==null) {
			createInstance();			
			ThreadWatchdog worker = new ThreadWatchdog(INSTANCE);
			workerThread = new Thread(worker);
			workerThread.setName("ThreadWatchdog worker");
			workerThread.start();
			logger.trace("Thread watchdog instance started. Worker: " + workerThread + 
					" (id " + workerThread.getId() + ")");
		}		
		return INSTANCE;
	}

	/**
	 * Adds a thread to be watched.
	 * @param thread (Thread) The thread to be watched.
	 * @param timeoutFromNow (long) Thread timeout in millis.
	 */
	public synchronized void addThread(Thread thread, long timeoutFromNow) {
	
		if(timeoutFromNow<=0) {
			logger.warn("Timeout must be non-zero positive. Ignoring thread.");
			return;
		}
		
		// Instantiating the thread expiration map, if needed to. It wasn't initialized previously to avoid a race 
		//	condition between getInstance() and addThread() (sometimes the watchdog loop would run before the first
		//	thread to watch was added, therefore killing the instance before it had the chance to do anything). 
		if(INSTANCE.threadExpirationMap==null)
			INSTANCE.threadExpirationMap = new HashMap<>();
		
		threadExpirationMap.put(thread, new Long(new Date().getTime() + timeoutFromNow));
		logger.trace("Watching for " + thread + " (id " + thread.getId() + "), timeout " + timeoutFromNow + ". " + 
				threadExpirationMap.size() + " threads being watched.");
	}

	/**
	 * Forgets about a thread, ignoring its current state (i.e. may keep running).
	 * @param thread (Thread)
	 */
	public synchronized void removeThread(Thread thread) {
		
		threadExpirationMap.remove(thread);
	}
			
	/* * Code below implementing the worker thread ****************************************************************** */
	public ThreadWatchdog(ThreadWatchdog parent) {
		
		this.parent = parent;		
	}
	
	@Override
	public void run() {
		try {
			// Running periodically until not needed anymore (*)
			while(true) {	
				// Check whether at least a thread to be watched was added after initialization; wait for next loop
				//	otherwise.
				if(parent.threadExpirationMap==null) {
					Thread.sleep(DEFAULT_LOOP_INTERVAL);
					continue;
				}
				
				// Next loop will be scheduled to run after DEFAULT_LOOP_INTERVAL millis (in order to watch for
				//	threads exited by themselves or interrupted externally). But... (**)
				long now = new Date().getTime();						
				long nextExpirationTime = now + DEFAULT_LOOP_INTERVAL;

				Thread[] watchedThreads = (Thread[])parent.threadExpirationMap.keySet().toArray(new Thread[0]);
				ArrayList<Thread> removalRequired = new ArrayList<>(); 				
				for(Thread thread : watchedThreads) {
										
					long threadExpirationTime = parent.threadExpirationMap.get(thread).longValue();
					
					// Checking for dead (exited by themselves), (externally)) interrupted and timed-out threads
					if(!thread.isAlive()) {
						removalRequired.add(thread);
						logger.trace("Forgetting about thread " + thread + " (id " + thread.getId() + "): is dead");
					}
					else if(thread.isInterrupted()) {
						removalRequired.add(thread);
						logger.trace("Forgetting about thread " + thread + " (id " + thread.getId() + 
								"): already interrupted");
					}
					else if(threadExpirationTime <= now) {
						thread.interrupt();
						removalRequired.add(thread);
						logger.trace("Thread " + thread + " (id " + thread.getId() + 
								") has been interrupted after specified timeout");
					}
					else
						// (**)... if a thread is due to timeout before DEFAULT_LOOP_INTERVAL, the next loop will be
						//	scheduled accordingly.
						nextExpirationTime = threadExpirationTime<nextExpirationTime?
								threadExpirationTime:
									nextExpirationTime;
				}
				
				// Removing finished dead and interrupted threads
				for(Thread toBeRemoved : removalRequired)
					parent.removeThread(toBeRemoved);
				
				// (*) Checking whether the instance should be kept running (it shouldn't when there're no more threads  
				//	to be watched).
				if(INSTANCE==null) {
					logger.trace("Instance was destroyed, exiting worker thread");
					return;
				}
				else if(INSTANCE.threadExpirationMap.size()==0) {
					logger.trace(removalRequired.size() + " threads removed in current loop, " +
							"no threads remaining, finalizing instance");
					INSTANCE = null;
					return;
				}
				else if(removalRequired.size()>0)
					logger.trace(removalRequired.size() + " threads removed in current loop, " + 
							parent.threadExpirationMap.size() + " remaining");
					
				// Scheduling next loop 
				Thread.sleep(nextExpirationTime-now);

			}
		}
		catch(InterruptedException e) {
			logger.warn("Weird! Thread watchdog loop was interrupted");			
		}
	}
	
	/* * Code below implementing singleton functionality ************************************************************ */
	
	private ThreadWatchdog() {
		
		// Just do nothing, simply prevents a default (public) constructor. 
	}
	
	private synchronized static void createInstance() {

		if (INSTANCE == null) {
            synchronized(ThreadWatchdog.class) {
                if (INSTANCE==null) { 
                    INSTANCE = new ThreadWatchdog();
                }
            }
        }
    }
	
	public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException("ThreatWatchdog is a singleton class"); 
	}
}
/* ****************************************************************************************************************** */