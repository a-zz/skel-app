/* ****************************************************************************************************************** *
 * StringUtil.java                                                                                                    *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

/**
 * Utility class for string handling and processing
 * @author a-zz
 */
public class StringUtil {

	/**
	 * Wraps a string to a certaing line lenght.
	 * 	<br/><br/>
	 * 	This is a quick & dirty function for wrapping codes, string keys and the like, not to be used for human-readable 
	 * 	texts as no smart word division is done at end of line. For better results, line-feeds contained in the input
	 * 	string are removed prior to wrapping. 
	 * @param text (String) The text to be wrapped.
	 * @param lineLenght (int) The resulting line length. 0 for no wrapping.
	 * @return (String) The wrapped text.
	 */
	public static String lineWrap(String src, int lineLenght) {
				
		src = src.replace(System.getProperty("line.separator"), " ");
		if(lineLenght==0)
			return src;
		
		String result = "";
		int line = 0;
		for(int i = 0; i<src.length(); i++) {
			line++;
			result += src.charAt(i);
			if(line==64) {
				result += System.getProperty("line.separator");
				line = 0;
			}
		}

		return result;
	}
}
