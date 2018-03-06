/* ****************************************************************************************************************** *
 * StrongPassword.java                                                                                                *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Handling of strong passwords
 * TODO Testing required
 * @author a-zz
 */
public class StrongPassword
{
	// Complexity requirements. These are default values; can be customized.
	// As static members, complexity requirements are set system-wide
	private static int minLenght = 8;
	private static int maxLength = 16;
	private static boolean lettersAndNumbersRequired = true;
	private static boolean upperAndLowerCaseRequired = true;
	private static boolean symbolsRequired = true;
	
	/**
	 * Encrypts a user-provided password into a secure password suitable for database storage
	 * @param clearTextPassword (String) The user's password
	 * @return (String) A secure 128-byte password consisting of:
	 * <ul>
	 * <li>a randomly-generated 64-byte salt</li>
	 * <li>the SHA-256 hash of the user's password plus salt (64-byte)</li> 
	 * </ul>
	 * Return value is null if the password coulnd't be encrypted, e.g. for not satisfaying complexity requirements
	 */
	public static String encrypt(String clearTextPassword) {
		
		// 0. First thing first: check complexity requirements
		if(!checkComplexityRequirements(clearTextPassword))
			return null;
		
		try {
			// 1. Salt mining
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[32];
			sr.nextBytes(salt);
			
			// 2. Password encrypting
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt);
			byte[] encryptedPassword = md.digest(clearTextPassword.getBytes());
			
			// 3. Returning the secure password
			StringBuilder result = new StringBuilder();
			for(int i=0; i< salt.length ;i++)
				result.append(Integer.toString((salt[i] & 0xff) + 0x100, 16).substring(1));
			for(int i=0; i< encryptedPassword.length ;i++)
				result.append(Integer.toString((encryptedPassword[i] & 0xff) + 0x100, 16).substring(1));
			
			return result.toString();
		}
		catch(NoSuchAlgorithmException e) {
			// This should never happen...
			return null;
		}
	}
	
	/**
	 * Checks a user-provided password against a (previosuly stored) secure password
	 * @param clearTextPassword (String) The password to check
	 * @param securePassword (String) The secure password, generated by encrypt()
	 * @return (boolean) true if both passwords match
	 * @see encrypt()
	 */
	public static boolean check(String clearTextPassword, String securePassword) {
		
		try	{
			// 1. Splitting the securepassword into salt and encrypted password 
			byte[] salt = extractHex(securePassword.substring(0, 64));
			String storedEncryptedPassword = securePassword.substring(64);
			
			// 2. Encrypting the user-provided clear-text password plus extracted salt
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt);
			byte[] encryptedPassword = md.digest(clearTextPassword.getBytes());
			
			// 3. Comparing
			StringBuilder sb = new StringBuilder();
			for(int i=0; i< encryptedPassword.length ;i++)
            {
				sb.append(Integer.toString((encryptedPassword[i] & 0xff) + 0x100, 16).substring(1));
            }				
			
			return sb.toString().equals(storedEncryptedPassword);			
		}
		catch(NoSuchAlgorithmException e) {
			// This should never happen...
			return false;
		}			
	}
			
	/**
	 * Checks a password for complexity requirements 
	 * @param clearTextPassword (String) The password to be checked
	 * @return (boolean) true if the password meets the requirements
	 */
	public static boolean checkComplexityRequirements(String clearTextPassword) {
		
		return 	lengthIsAcceptable(clearTextPassword) &&
				(!lettersAndNumbersRequired || hasLettersAndNumbers(clearTextPassword)) &&
				(!upperAndLowerCaseRequired || hasUpperAndLowerCase(clearTextPassword)) &&
				(!symbolsRequired 			|| hasSymbols(clearTextPassword));
	}

	/**
	 * Checks for length requirements 
	 * @param clearTextPassword (String) The password to be checked
	 * @return (boolean)
	 */
	public static boolean lengthIsAcceptable(String clearTextPassword) {
		
		return clearTextPassword.length()>=minLenght &&
				clearTextPassword.length()<=maxLength;
	}
	
	/**
	 * Checks for letters and numbers 
	 * @param clearTextPassword (String) The password to be checked
	 * @return (boolean)
	 */
	public static boolean hasLettersAndNumbers(String clearTextPassword) {
		
		Pattern letters	= Pattern.compile("\\w");
		Pattern numbers	= Pattern.compile("\\d");
		
		return 	letters.matcher(clearTextPassword).find() && 
				numbers.matcher(clearTextPassword).find();				
	}	

	/**
	 * Checks for upper and lower case letters   
	 * @param clearTextPassword (String) The password to be checked
	 * @return (boolean)
	 */
	public static boolean hasUpperAndLowerCase(String clearTextPassword) {
		
		Pattern upperCase = Pattern.compile("\\p{javaUpperCase}");
		Pattern lowerCase = Pattern.compile("\\p{javaLowerCase}");
		return 	upperCase.matcher(clearTextPassword).find() && 
				lowerCase.matcher(clearTextPassword).find();				
	}	

	/**
	 * Checks for symbos   
	 * @param clearTextPassword(String) The password to be checked
	 * @return (boolean)
	 */
	public static boolean hasSymbols(String clearTextPassword) {
		
		Pattern symbols = Pattern.compile("\\p{Punct}");
		return	symbols.matcher(clearTextPassword).find();
	}
	
	/**
	 * Returns the minimum length currently required.  
	 * @return (int)
	 */
	public static int getMinLength() {
		
		return minLenght;
	}

	/**
	 * Sets the minimum length for passwords. It's set statically and thus system-wide. Default is 8 chars.
	 * @param minLength (int) 
	 */
	public static void setMinLength(int minLength) {
		
		StrongPassword.minLenght = minLength;
	}

	/**
	 * Gets the maxnimum length currently required 
	 * @return (int)
	 */
	public static int getMaxLength() {
		
		return maxLength;
	}

	/**
	 * Sets the maximum length for passwords. It's set statically and thus system-wide. Default is 8 chars.
	 * @param maxLength (int) 
	 */	
	public static void setMaxLength(int maxLength) { 
	
		StrongPassword.maxLength = maxLength;
	}

	/**
	 * Tells whether letters and numbers are required
	 * @return (boolean)
	 */
	public static boolean getLettersAndNumbersRequired() {
		
		return lettersAndNumbersRequired;
	}

	/**
	 * Sets the "has letters and numbers" requirement. It's set statically and thus system-wide. Default is true.
	 * @param requires (boolean) 
	 */
	public static void setLettersAndNumbersRequires(boolean required) {
		
		StrongPassword.lettersAndNumbersRequired = required;
	}

	/**
	 * Tells wether upper and lower case letters are required
	 * @return (boolean)
	 */
	public static boolean getUpperAndLowerCaseRequired() {
		
		return upperAndLowerCaseRequired;
	}

	/**
	 * Sets the "has upper and lower case" requirement. It's set statically and thus system-wide. Default is true.
	 * @param required (boolean) 
	 */
	public static void setUpperAndLowerCaseRequired(boolean required) { 
		
		StrongPassword.upperAndLowerCaseRequired = required;
	}

	/**
	 * Tells wether symbols are required
	 * @return (boolean)
	 */
	public static boolean getSymbolsRequired() {
		
		return symbolsRequired;
	}

	/**
	 * Sets the "has symbols" requirement. It's set statically and thus system-wide. Default is true.
	 * @param required (boolean) 
	 */
	public static void setSymbolsRequired(boolean required) {
		
		StrongPassword.symbolsRequired = required;
	}
	
    private static byte[] extractHex(String hex) {
    	
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        	bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }	
}
/* ****************************************************************************************************************** */