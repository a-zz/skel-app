/* ****************************************************************************************************************** *
 * Base64Serializer.java                                                                                              *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 * Utility class for Base64 serializing (and deserializing)
 * @author a-zz
 */
public class Base64Serializer {

	/**
	 * Serializes binary content
	 * @param input (byte[])
	 * @return (String) Base64 encoded serialized content
	 */
	public static String serialize(byte[] input) {
	
		return Base64.getEncoder().encodeToString(input);
	}
	
	/**
	 * Serializes an object. 
	 * @param input (Object)
	 * @return (String) Base64 encoded serialized object
	 * @throws IOException
	 */
	public static String serialize(Serializable input) throws IOException {
	
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
	        baos = new ByteArrayOutputStream();
	        oos= new ObjectOutputStream( baos );
	        oos.writeObject(input);
	        return serialize(baos.toByteArray());
        }
        finally {
        	if(oos!=null)
        		oos.close();
        	if(baos!=null)
        		baos.close();
        }
	}

	/**
	 * Serializes a gile
	 * @param input (File)
	 * @return (String) Base64 encoded serialized file
	 * @throws IOException
	 */
	public static String serialize(File input) throws IOException {
		
		return serialize(FileUtil.readBinary(input));
	}

	/**
	 * Deserializes a Base64 string as binary content
	 * @param input (String)
	 * @return (byte[])
	 */
	public static byte[] deserializeAsContentByte(String input) {
		
		return Base64.getDecoder().decode(input.getBytes());
	}

	/**
	 * Deserializes a Base64 string as an object
	 * @param input (String)
	 * @return (Object)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserializeAsObject(String input) throws IOException, ClassNotFoundException {
		
    	ObjectInputStream ois = null;
    	try {
	    	ois = new ObjectInputStream(
	    			new ByteArrayInputStream(
	    					deserializeAsContentByte(input)));
	    	Object objeto = ois.readObject();
	    	return objeto;
    	}
    	finally {
    		if(ois!=null)
    			ois.close();
    	}
	}
		
	/**
	 * Deserializes a Base64 string into a file
	 * @param input (String)
	 * @param dst (File)
	 * @throws IOException
	 */
	public static void deserializeAsFile(String input, File dst) throws IOException {
		
		FileUtil.writeBinary(dst, deserializeAsContentByte(input));
	}
}
/* ****************************************************************************************************************** */