/* ****************************************************************************************************************** *
 * DateTimeUtil.java                                                                                                  *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

/**
 * Utiliy class for date & time handling and processing
 * @author a-zz
 */
public class DateTimeUtil {

	/**
	 * Gets a human-readable representation of an elapsed time given in milliseconds (the substraction of two Date
	 * 	objects, e.g.). It's not intended for accuracy but for informative purposes; also, it doesn't know about the
	 * 	exact start time and thus leap years and the like aren't considered. For these same reasons, the month length is 
	 * 	taken as 30 days. 
	 * @param millis (long) The elapsed time in milliseconds; negatives values aren't allowed. The equivalent to the 
	 * 	maximum value allowed for a long is 296 533 308 years, so don't use this method for cosmological purposes :P
	 * @param includeMillis (boolean) Sets whether the return string should also include milliseconds
	 * @return (String) The elapsed time in the form "1y 1m 1d 01:01:01.001". Zero values to the left are ommitted,
	 * 	except for minutes and seconds that are always shown (i.e. at least "00:00" is returned). 
	 */
	public static String humanReadableElapsedTime(long millis, boolean includeMillis) {
		
		if(millis<0)
			throw new IllegalArgumentException("Elapse time can't be negative");
		
		long[] durtns = {1000, 60, 60, 24, 30, 12}; 
		long[] values = new long[7];	
		long remaindr = millis;
		int i;
		for(i = 0; i<6; i++) {
			values[i] = remaindr % durtns[i];
			remaindr = (remaindr-values[i])/durtns[i];
			if(remaindr==0)
				break;
		}			
		values[6] = remaindr;
		
		return 	((values[6])!=0?values[6] + "y ":"") +
				((values[6]!=0 || values[5]!=0)?values[5] + "m ":"") +
				((values[6]!=0 || values[5]!=0 || values[4]!=0)?values[4] + "d ":"") +
				((values[6]!=0 || values[5]!=0 || values[4]!=0 || values[3]!=0)?
						String.format("%02d", values[3]) + ":":"") +
				String.format("%02d", values[2]) + ":" + 
				String.format("%02d", values[1]) +
				(includeMillis?"."+String.format("%03d", values[0]):"");
	}
}
/* ****************************************************************************************************************** */