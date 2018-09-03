/* ****************************************************************************************************************** *
 * RunnableTask.java                                                                                                  *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.util.ArrayList;
import java.util.Date;

/**
 * An abstract class extending the abilites of Runnable with monitorizable progress status and richer execution control.
 * @author a-zz
 */
public abstract class RunnableTask implements Runnable {

	private Date startTime = null;
	private Date endTime = null;
	
	public enum EnumExecutionStatus { UNINITIALIZED, RUNNING, PAUSED, STOPPED, ENDED }	
	/**
	 * Overall execution status. Implementing sublasses should check this to know when to pause, resume and stop.
	 */
	protected EnumExecutionStatus executionStatus = EnumExecutionStatus.UNINITIALIZED;
	
	private int pc = 0;
	private ArrayList<String> statusLog = new ArrayList<>();	
		
	/**
	 * Updates the progress status. Implementing subclasses will need to call this method when appropiate to reflect
	 * 	the related task progress. Also sets the execution status and start and end times (see pc argument).
	 * @param pc (int) Percentage completed, with the following nuances:
	 * 	<ul>
	 * 	<li>Only values 0-100 (included) are allowed. Othersise an IllegalArgumentException is thrown.</li>
	 * 	<li>Progress percentage can only grow. Regressive progress is punished with an IllegalArgumentException.</li>
	 * 	<li>When pc is 100, the execution status is set to ENDED and endTime is recorded.</li>
	 * 	<li>Otherwise, the execution status is set to RUNNING and startTime is recorded, if it wasn't by now.</li>
	 * 	</ul>
	 * Therefore, is advisable to call statusProgressUpdate(0, "(whatever message)"); at the start of the implementing
	 * 	class run() method to get the execution status and startTime properly initialized. 
	 * @param status (String) Status description message
	 * @return (String) A status string in the format "[ ##%] status_message" (for fedding into a logging utility, e.g.)
	 */
	public String statusProgressUpdate(int pc, String status) {
		
		if(pc<0 || pc>100)
			throw new IllegalArgumentException("Only values from 0 to 100 (both included) are allowed");
		else if(pc<this.pc)
			throw new IllegalArgumentException("Progress percentage can't go backwards");
		else if(pc==100) {
			this.executionStatus = EnumExecutionStatus.ENDED;
			this.endTime = new Date();
		}
		else {
			this.executionStatus = EnumExecutionStatus.RUNNING;
			if(this.startTime == null)
				this.startTime = new Date(); 
		}
		
		this.pc = pc;
		String result = "[" + String.format("%3s", Integer.toString(pc)) + "%] " + status;
		this.statusLog.add(result);
		return result;
	}
	
	/**
	 * Gets the current status.
	 * @param withPc (boolean) Sets whether the current progress percentage should be prepended to the status message 
	 * @return (String) The current status message. If withPc is true, the percentage is prepended with "[ ##%] ".
	 */
	public String getCurrentStatus(boolean withPc) {
		
		String result = this.statusLog.get(this.statusLog.size()-1);
		
		if(!withPc)
			result = result.substring(new String("[000%] ").length());
		
		return result;
	}

	/**
	 * Gets the progress percentage.
	 * @return (int)
	 */
	public int getProgressPc() {
		
		return this.pc;
	}
	
	/**
	 * Gets the complete list of progress status messages, ordered from older to newer 
	 * @return (ArrayList<String>) An array list of strings with the format "[ ##%] status_message".
	 */
	public ArrayList<String> getStatusLog() {
		
		return this.statusLog;
	}	
	
	/**
	 * Pauses the task associated to the implementing class. Actually: the implementing class should check this (via the
	 *  protected executionStatus member or public getExecutionStatus() method) and take proper action.
	 */
	public void pause() {
		
		this.executionStatus = EnumExecutionStatus.PAUSED;
	}
	
	/**
	 * Resumes the task associated to the implementing class. Actually: the implementing class should check this (via 
	 *  the protected executionStatus member or public getExecutionStatus() method) and take proper action.
	 */
	public void resume() {
		
		this.executionStatus = EnumExecutionStatus.RUNNING;
	}
	
	/**
	 * Stops the task associated to the implementing class by sending an interruption and recording endTime. The 
	 * 	overriden run() method in the implementing classes should often check for Thread.interrupted() and catch 
	 * 	InterruptedException to address this event.
	 */
	public void stop() {
		
		this.executionStatus = EnumExecutionStatus.STOPPED;
		this.endTime = new Date();
		Thread.currentThread().interrupt();
	}
	
	/**
	 * Gets the current execution status.
	 * @return (EnumExecutionStatus)
	 */
	public EnumExecutionStatus getExecutionStatus() {
		
		return this.executionStatus;
	}
	
	/**
	 * Gets the task start time.
	 * @return (Date)
	 */
	public Date getStartTime() {
		
		return this.startTime;
	}
	
	/**
	 * Gets the task end time.
	 * @return (Date)
	 */
	public Date getEndTime() {
		
		return this.endTime;
	}
	
	/**
	 * Gets a human-readable representation of the task running time. If the execution status is ENDED or STOPPED, the  
	 * 	total elapsed time is returned (from startTime to endTime). Otherwise, the currently elapsed time is returned 
	 * 	(from startTime to now).
	 * @param includeMillis (boolean) Sets whether the return string should also include milliseconds
	 * @return (String)
	 */
	public String getRunningTime(boolean includeMillis) {
	
		return (executionStatus==EnumExecutionStatus.ENDED || executionStatus==EnumExecutionStatus.STOPPED)?
				DateTimeUtil.humanReadableElapsedTime(endTime.getTime()-startTime.getTime(), includeMillis):
					DateTimeUtil.humanReadableElapsedTime(new Date().getTime()-startTime.getTime(), includeMillis);
	}
}
/* ****************************************************************************************************************** */