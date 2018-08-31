/* ****************************************************************************************************************** *
 * ServerTask.java                                                                                                    *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.azz.logging.AppLogger;

/**
 * Utility class for running OS commands.
 * @author a-zz
 * TODO Production-grade testing needed!
 */
public class ServerTask implements Runnable {

	private static AppLogger logger = new AppLogger(ServerTask.class);
	
	private String cmdLine;
	private Process p;
	private BufferedReader stdout;
	private BufferedReader stderr;
	
	private int exitCode = -1;
	
	private ServerTask parent;
	
	/**
	 * Constructor: creates a server task, but won't start until the start() method is called.  
	 * @param cmd (String) The task command line.
	 */
	public ServerTask(String cmdLine) {
		
		this.cmdLine = cmdLine;
	}
	
	/**
	 * Starts the task. 
	 * @param waitForTaskExit (boolean) Sets wether the execution will wait for the task to exit or else it'll be
	 * 	launched in a separate daemon thread and return immediately. 
	 * 	<br/><br/>
	 * If true, the method will wait for the process to exit and then return its exit code. The complete stdout & stderr 
	 * 	can be get from the getStodut() and getStderr() methods.  
	 * <br/><br/>
	 * Otherwise, the method will return immediately with -1. It's up to the caller to check the task exit code 
	 * 	(getExitCode()) afterwards. While the task is running, stdout & stderr can be checked regularly to get new 
	 * 	output (e.g. task progress information).
	 * @param timeout (long) When running as a separate thread (waitForTaskExit==false), sets a timeout (in millis) for 
	 * 	the thread to run. Ignored if waitForTaskExit==true; also if negative or zero.
	 * @return (int) The task exit code or -1, if waitForTaskExit was set to false.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public int start(boolean waitForTaskExit, long timeout) throws IOException, InterruptedException {		
		
		if(waitForTaskExit && timeout>0)
			logger.warn("A timeout only makes sense when running as a separate thread (waitForTaskExit==false)");		
		
		p = Runtime.getRuntime().exec(cmdLine);
		stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
		stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		if(waitForTaskExit) {
			// Launching and waiting for exit
			logger.debug("About to launch server task: " + cmdLine);
			exitCode = p.waitFor();
			logger.trace("Task exited with code: " + exitCode);
			return exitCode;
		}
		else {
			// Launching non-bloacking thread
			ServerTask worker = new ServerTask(this);
			Thread workerThread = new Thread(worker);
			workerThread.setName("cmd:" + (cmdLine.length()>16?cmdLine.substring(0, 13) + "...":cmdLine));
			workerThread.setDaemon(true);
			workerThread.start();
			logger.debug("Server task launched as separate thread: " + workerThread + 
					" (id " + workerThread.getId() + ")");
			if(timeout>0) 
				ThreadWatchdog.getInstance().addThread(workerThread, timeout);			
			return -1;
		}
	}

	/**
	 * Gets the task stdout
	 * @return (BufferedReader)
	 */
	public BufferedReader getStdout() {
		
		return stdout;
	}
	
	/**
	 * Gets the task stderr
	 * @return (BufferedReader)
	 */
	public BufferedReader getStderr() {
		
		return stderr;
	}

	/**
	 * Gets the task exit code.
	 * @return (int) Possible values are:<ul>
	 * 	<li>When waitForTaskExit was set to false:</li><ul>
	 * 		<li>-1 means the task's still running.</li>
	 * 		<li>-2 means the task was killed (for whatever reason, but possibly because of specified timeout).</li>
	 * 	</ul>
	 * 	<li>In any case, the task exit code is returned once the task has exited normally.</li>
	 * </ul>
	 */
	public int getExitCode() {
		
		return exitCode;
	}
	
	/* * Code below implementing the non-blocking thread ************************************************************ */
	
	private ServerTask(ServerTask parent) {
		
		this.parent = parent;
	}
	
	@Override
	public void run() {		
		try {
			parent.exitCode = parent.p.waitFor();
		}
		catch(InterruptedException e) {
			parent.p.destroy();
			parent.exitCode = -2;
			logger.warn("Server task " + Thread.currentThread() + " (id " + Thread.currentThread().getId() + 
					") was interrupted!");
		}
	}
}
/* ****************************************************************************************************************** */
