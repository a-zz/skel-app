/* ****************************************************************************************************************** *
 * FileUtil.java                                                                                                      *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Management of directories intended for temporary file storage. 
 * <br/><br/>
 * Temporary files are automatically deleted according to a predefined ttl (time-to-live). As different consumer 
 * utilites may require diferent ttl's, multiple temporary directories can be created, each one with a different ttl.
 * This class keeps track of the temp dirs created, identifying each other by a user defined key.
 * @author a-zz
 */
public class TmpDir {

	private File dir;
	private long ttl;
	private static HashMap<String,TmpDir> tmpDirs = new HashMap<String,TmpDir>();
		
	/**
	 * Constructor: makes and registers a temporary directory
	 * @param key (String) The key assigned to the new directory, required for directory usage. 
	 * @param dir (File) The directory to be used
	 * @param create (boolean) Sets if the directory should be created, in case it doesn't previously exist.
	 * @param ttl (long) Time-to-live for files held in the temporary dir, in milliseconds. Files older than that 
	 * 	(strictly: files with last-modified date older than the ttl) will be purged on the next directory operation. 
	 * 	Set to 0 for indefinite time-to-live (no purging). 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public TmpDir(String key, File dir, boolean create, long ttl) throws IOException, IllegalArgumentException {
		
		// Checking for duplicates
		if(tmpDirs.containsKey(key))
			throw new IllegalArgumentException("A temp dir already exists by the key " + key);
		for(TmpDir prev : tmpDirs.values())
			if(dir.equals(prev.dir))
				throw new IllegalArgumentException("A temp dir already exists for path " + dir.getAbsolutePath());
		
		// Creating the directory, if needed to		
		if(!dir.exists() && create && !dir.mkdirs())
			throw new IOException("Couldn't create temp dir " + dir.getAbsolutePath());
		
		// Registering
		this.dir = dir;
		this.ttl = ttl;
		tmpDirs.put(key, this);
	}

	/**
	 * Gets a temporary file. Older files (according to temp dir ttl) will be purged before creating the new one.
	 * @param ext (String) Extension for the file. If null, ".tmp" will be used.
	 * @return (File) The new temporary file created.
	 * @throws IOException
	 */
	public File getTempFile(String ext) throws IOException {
		
		ext = ext!=null?ext:"tmp";
		purgeOldFiles();
		return File.createTempFile(Long.toString(new Date().getTime()), "." + ext, dir);
	}
	
	/**
	 * Gets a temporary file from a temp dir. Older files (according to temp dir ttl) will be purged before creating the
	 * 	new one.
	 * @param dirKey (String) The key referring to the temp dir.
	 * @param ext (String) Extension for the file. If null, ".tmp" will be used.
	 * @return (File) The new temporary file created.
	 * @throws IOException
	 */
	public static File getTempFile(String dirKey, String ext) throws IOException {
		
		return tmpDirs.get(dirKey).getTempFile(ext);
	}
	
	private void purgeOldFiles() {
		
		if(ttl==0)
			return;
		
		File[] dirContents = dir.listFiles();
		for(File item: dirContents) 
			if(new Date().getTime() - item.lastModified() > ttl)
				item.delete();
	}
}
/* ****************************************************************************************************************** */