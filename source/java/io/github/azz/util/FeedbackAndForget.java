/* ****************************************************************************************************************** *
 * FeedbackAndForget.java                                                                                             *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.lang.reflect.Method;

import io.github.azz.logging.AppLogger;

/**
 * This class serves just one purpose: calling a method with two arguments (String, Object)  in a separate thread.
 * <br/><br/>
 * Although this may come handy in different situations, it's primarily intended for classes provinding feedback to 
 * 	other classes in an asynchronous fashion: running in a separate thread avoids the data-producing class having to 
 * 	wait for the data-receiving method to return.
 * <br/><br/>
 * E.g. classes DaemonMonitor and TextFileMonitor require this functionality in order to avoid a slow data-receiving  
 * 	method to block the whole polling loop.	
 * @author a-zz
 * TODO Consider adding a timeout argument to send() method to avoid thread accumulation.
 */
public class FeedbackAndForget implements Runnable {

	private static AppLogger logger = new AppLogger(FeedbackAndForget.class);
	
	private Object targetObject;
	private Method targetMethod;
	private String senderId;
	private Object data;	
	
	/**
	 * Sends data to a target object.method(String, Object) asynchronously. I.e. runs object.method(String, Object) not 
	 * 	waiting for it to return.
	 * @param targetObject (Object) The target object.
	 * @param targetMethod (Method) The target method. It must have two arguments:<ul>
	 * 	<li>(String) A unique ID for the receiving method to identify the sender.</li>
	 * 	<li>(Object) A data object.</li>
	 * </ul>
	 * @param senderId (String) A unique ID for the receiving method to identify the sender.
	 * @param data (Object) The data to send.
	 * @param timeout (long) Operation timeout (in millis). The execution thread is interrupted after this time. Ignored
	 * 	if negative or zero. 
	 */
	public static void send(Object targetObject, Method targetMethod, String senderId, Object data, long timeout) {
		
		FeedbackAndForget worker = new FeedbackAndForget(targetObject, targetMethod, senderId, data);
		Thread workerThread = new Thread(worker);
		if(timeout>0)
			ThreadWatchdog.getInstance().addThread(workerThread, timeout);			
		workerThread.start();
	}
	
	/* * Code below implementing the worker thread ****************************************************************** */ 
	
	private FeedbackAndForget(Object targetObject, Method targetMethod, String senderId, Object data) {
				
		this.targetObject = targetObject;
		this.targetMethod = targetMethod;
		this.senderId = senderId;
		this.data = data;
	}
	
	@Override
	public void run() {
		try {
			targetMethod.invoke(targetObject, senderId, data);
			logger.trace("Target method invoked, sender id " + senderId);
		}
		catch(Exception e) {
			logger.error("Couldn't invoke target method for sender id " + senderId + ": " + e.getMessage());
		}
	}	
}
/* ****************************************************************************************************************** */
