/* ****************************************************************************************************************** *
 * FileUtil.java                                                                                                      *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang3.StringUtils;

import io.github.azz.config.LocalConfiguration;
import io.github.azz.logging.AppLogger;

/**
 * Miscelaneous file utilities
 * @author a-zz
 */
public class FileUtil {

	private static AppLogger logger = new AppLogger(FileUtil.class); 
	
	/**
	 * Reads a file as binary
	 * @param src (File) The file to be read
	 * @return (byte[]) The binary content of the file
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public static byte[] readBinary(File src) throws FileNotFoundException, IOException {
		
		if(!checkLocalServerFileSizeLimit(src))
			throw new IOException("Source file size exceeds local server limit; refused to read: " + src);
		
    	InputStream is = null;   	 
    	try {
    		is = new FileInputStream(src);
	    	long length = src.length();
	    	byte[] bytes = new byte[(int)length];
	    	int offset = 0;
	    	int numRead = 0;
	    	while (offset < bytes.length
	    			&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) 
	    		offset += numRead;
	    	is.close();
	    	is = null;
	    	if (offset < bytes.length) 
	    		throw new IOException("Error reading binary file " + src.getName());
	    	     	
	    	logger.trace("Read binary file " + src + " (" + bytes.length + " bytes)");
	    	return bytes;
    	}
    	catch(IOException e) {
    		throw e;
    	}
    	finally {
    		if(is!=null)
    			is.close();
    	}
	}

	/**
	 * Reads a file as text
	 * @param src (File) The File to be read
	 * @return (String) The text content of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readText(File src) throws FileNotFoundException, IOException {
	
		if(!checkLocalServerFileSizeLimit(src))
			throw new IOException("Source file size exceeds local server limit; refused to read: " + src);
		
		StringBuffer sb = new StringBuffer(1000);
		BufferedReader reader = null;		
		try {
			reader = new BufferedReader(new FileReader(src));
			
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				sb.append(readData);
				buf = new char[1024];
			}		
			reader.close();
			reader = null;
			logger.trace("Read text file " + src + " (" + sb.toString().length() + " characters)");
			return sb.toString();
		}
		finally {
			if(reader!=null)
				reader.close();
		}
	}

	/**
	 * Checks whether a file could be read into memory (by readBinary() or readText() methods), according to local
	 * 	server property server.file.read.limit
	 * @param src (File) The file checked
	 * @return (boolean) true if the file size is smaller or equal than the limit set
	 * @throws IOException
	 */
	public static boolean checkLocalServerFileSizeLimit(File src) throws IOException {
		
		 return src.length()<=Long.parseLong(LocalConfiguration.getProperty("server.limit.file.read"));
	}
	
	/**
	 * Writes binary data to a file
	 * @param dst (File) The destination file. Overwritten if previoulsy exists.
	 * @param content (byte[]) The data to be written.
	 * @throws IOException
	 */
	public static void writeBinary(File dst, byte[] content) throws IOException {
		
		if(!dst.exists())
			dst.createNewFile();
		
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(dst));
			os.write(content);
			logger.trace("Written binary file " + dst + " (" + content.length + " bytes)");
		}
		finally {
			os.close();
		}
	}

	/**
	 * Writes text to a file
	 * @param dst (File) The destination file. Overwritten if previoulsy exists.
	 * @param text (String) The text to be written.
	 * @throws IOException
	 */
	public static void writeText(File dst, String text) throws IOException {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(dst));
			writer.write(text);
			logger.trace("Written text file " + dst + " (" + text.length() + " characters)");
		}
		finally {
			writer.close();
		}
	}

	/**
	 * Copies files and directories. Behaviour is similar to the usual cp console command (always overwriting files and 
	 * 	blending directories). 
	 * @param src (File) The source file or directory.
	 * @param dst (File) The destination file or directory. 
	 * @throws IllegalArgumentException 
	 */
	public static void copy(File src, File dst) throws IllegalArgumentException, IOException {
		
		if(src.equals(dst))
			throw new IllegalArgumentException("Can't copy a file or directory to itself: " + src);
		
		if(!src.isDirectory() && !dst.isDirectory()) {			
			// Regular file to regular file
			boolean overwrite = true;
			if(!dst.exists()) {
                dst.createNewFile();
                overwrite = false;
			}        
			copyFile(src, dst);
			logger.trace("Copied file to file (overwrite: " + overwrite + "): " + src + " -> " + dst);
		}
		else if(!src.isDirectory() && dst.isDirectory()) {
			// Regular file to directory
			File dstFile = new File(endPath(dst.getAbsolutePath()) + src.getName());
			boolean overwrite = true;
			if(!dstFile.exists()) {
				dstFile.createNewFile();
				overwrite = false;
			}
			copyFile(src, dstFile);
			logger.trace("Copied file to directory (overwrite: " + overwrite + "): " + src + " -> " + dstFile);
		}
		else if(src.isDirectory() && dst.isDirectory()) {
			// Directory as subdirectory
			File dstDir = new File(endPath(dst.getAbsolutePath()) + src.getName());
			boolean blend = true;
			if(!dstDir.exists()) {
				dstDir.mkdir();
				blend = false;
			}
			copyDir(src, dstDir);
			logger.trace("Copied directory to directory (blend: " + blend + "): " + src + " -> " + dstDir);
		}
		else {
			// Directory to regular file: no way Jose!
			throw new IllegalArgumentException("Can't copy directory " + src + " to file " + dst);
		}
	}

	private static void copyFile(File src, File dst) throws IOException {

        if(!dst.exists())
            dst.createNewFile();
        
        InputStream is = null;
        OutputStream os = null;

        try {
        	is = new FileInputStream(src);
        	os = new FileOutputStream(dst);
        	byte[] buf = new byte[1024];
        	int len;
        	while ((len = is.read(buf)) > 0)
        		os.write(buf, 0, len);
        }
        finally {
        	if(is!=null)
        		is.close();
        	if(os!=null)
        		os.close();        	
        }
	}
	
	private static void copyDir(File src, File dst) throws IOException {
		
		File[] srcContent = src.listFiles();
		for(File item : srcContent) {
			if(item.isDirectory()) {
				String dstSubPath = endPath(dst.getAbsolutePath()) + item.getName();
				File dstSub = new File(dstSubPath);
				dstSub.mkdir();
				copyDir(item, dstSub);
			}
			else {
				String dstFilePath = endPath(dst.getAbsolutePath()) + item.getName();
				File dstFile = new File(dstFilePath);
				dstFile.createNewFile();
				copyFile(item, dstFile);
			}
		}
	}
	
	/**
	 * Deletes a file or directory
	 * @param target (File) The file or directory to be deleted
	 * @param recursive (boolean) Sets if recursive deletion should be done. When deleting a directory
	 * 	<ul>
	 * 	<li>If recursive is set, target and all its subdirs will be deleted.</li>
	 * 	<li>If not set, no deletion will be done.</li>
	 * 	</ul>
	 * 	It has no effect when deleting a regular file. 
	 */
	public static void delete(File target, boolean recursive) {

		if(target.isDirectory()) {
			if(recursive) {
				File[] contents = target.listFiles();
				for(File item : contents) {
					if(item.isDirectory())
						delete(item, true);
					else {						
						item.delete();
						logger.trace("Deleted file " + item);
					}
				}
				target.delete();
				logger.trace("Deleted directory " + target);
			}
			else 
				logger.trace("Deletion aborted: " + target + " is a directory and recursive flag wasn't set");			
		}
		else {
			target.delete();
			logger.trace("Deleted file " + target);
		}
	}

	/**
	 * Suffixes the path representation with the current filesystem directory separator, if needed  
	 * @param path (String) The path representation
	 * @return (String)
	 */
	public static String endPath(String path) {
		
		if(path.endsWith(File.separator))
			return path;
		else
			return path + File.separator;
	}	
	
	/**
	 * Gets the MIME content type of a file.
	 * 	<br/><br/>
	 * 	Uses the MIME type map provided by the JRE. It can be extended in META-INF/mime.types.  
	 * @param target (File)
	 * @return (String)
	 */
	public static String getContentType(File target) {
		
		return new MimetypesFileTypeMap().getContentType(target);
	}

	/**
	 * Compress a directory into a ZIP file
	 * @param srcDir (File) The source directory
	 * @param dstZip (File) The destination ZIP file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public static void zipDirectory(File srcDir, File dstZip) throws FileNotFoundException, IOException {
    	
    	ZipOutputStream zos = null;
    	try {
    		zos = new ZipOutputStream(new FileOutputStream(dstZip));
    		zipDirectory(srcDir, zos, endPath(srcDir.getAbsolutePath()));
    		logger.trace("Directory zipped: " + srcDir + " -> " + dstZip);
    	}
    	finally {
    		if(zos!=null)
    			zos.close();
    	}
    }
    
    private static void zipDirectory(File srcDir, ZipOutputStream zos, String rootPath) 
    		throws FileNotFoundException, IOException { 
    	
    	String[] contentNames = srcDir.list();
    	if(contentNames.length==0)
    		writeZipEntry(srcDir, zos, rootPath);
    	else 
	        for(String itemName : contentNames) { 
	            File item = new File(srcDir, itemName); 
	            if(item.isDirectory())
	            	zipDirectory(item, zos, rootPath);
	            else
	            	writeZipEntry(item, zos, rootPath);
	        }
    }
    
    private static void writeZipEntry(File item, ZipOutputStream zos, String rootPath) 
    		throws FileNotFoundException, IOException {
    	
    	byte[] buf = new byte[2156]; 
        int bytesIn = 0;
        FileInputStream fis = null;         
        try {            
            ZipEntry zEntry = item.isDirectory()?
            		new ZipEntry(endPath(item.getPath()).substring(rootPath.length())):
            		new ZipEntry(item.getPath().substring(rootPath.length()));
            zos.putNextEntry(zEntry);
            if(!item.isDirectory()) {
            	fis = new FileInputStream(item);          
            	while((bytesIn = fis.read(buf)) != -1) 
            		zos.write(buf, 0, bytesIn);
            }
        }
        finally {
        	if(fis!=null)
        		fis.close();
        }
    }
	
    /**
     * Splits a file name 
     * @param fileName (String) 
     * @return (String[]) A vector with components:
     *  <br/>[0] file base name
     *  <br/>[1] file extension
     */
    public static String[] splitFileName(String fileName) {
    	
    	String[] result = new String[2];    	  
    	
    	int dotAt = fileName.lastIndexOf(".");
    	if(dotAt==-1) {
    		result[0] = fileName;
    		result[1] = "";
    	}
    	else {
    		result[0] = fileName.substring(0, dotAt);
    		result[1] = fileName.substring(dotAt+1);
    	}
    	
    	return result;
    }
    
    /**
     * Sanitizes a file name to avoid trouble in the underlying file system. In order to make it widely portably,
     * 	anything but english letters, numbers, dots and hyphens is replaced by underscores. However, letters with
     * 	diacritics are replaced by their closest english letters, in order to make the sanitized name more readable.
     * @param fileName (String) The potentially dirty and harmful file name. 
     * @param caseSensitive (boolean) Set it to false if the underlying file system is not case sensitive; should this 
     * 	be it, the file name will be converted into lower case.
     * @return (String)
     */
    public static String sanitizeFileName(String fileName, boolean caseSensitive) {
    	
    	// Replacing letters with diacritics
    	fileName = StringUtils.stripAccents(fileName);
    	
    	// Converting to lower case, if needed to
    	if(!caseSensitive)
    		fileName = fileName.toLowerCase();
    	
    	// Replacing everything but english letters, numbers, dots and hyphens with underscores
    	fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    	
    	// Collapsing groups of underscores posibly produced from previous replacement into single underscores
    	fileName = fileName.replaceAll("_+", "_");
    	
    	return fileName;
    }
}
/* ****************************************************************************************************************** */