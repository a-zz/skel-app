/* ****************************************************************************************************************** *
 * Reflection.java                                                                                                    *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Java reflection utilities
 * @author a-zz
 */
public class Reflection {

	/**
	 * Builds a list of classes found under a package.
	 * @param packageName (String) The package name
	 * @param recurseSubPackages (boolean) Sets if subpackages should also be scanned.
	 * @param foundClassesList (ArrayList<String>) A list with the names of classes found.
	 * @param superClassNameFilter (String) An optional filter: lists only the classes extending or implementing this
	 * 	class (except for the class itself). No filter applied if null.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void scanPackage(String packageName, boolean recurseSubPackages, 
			ArrayList<String> foundClassesList, String superClassNameFilter) throws ClassNotFoundException {
				
		Class superClassFilter = superClassNameFilter!=null?Class.forName(superClassNameFilter):null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		// Loading package as a directory
		URL packageUrl = classLoader.getResource(packageName.replace(".", "/"));
		if(packageUrl==null)
			throw new ClassNotFoundException("Package " + packageName + " doesn't exists"); 
		File packageDir = new File(packageUrl.getFile());
		File[] packageContents = packageDir.listFiles();
		
		// Checking package contents
		for(File item : packageContents) {
			if(item.isDirectory() && recurseSubPackages) {
				// Recursing into subpackage
				String subPackageName = packageName + "." + item.getName();
				scanPackage(subPackageName, recurseSubPackages, foundClassesList, superClassNameFilter);
			}
			else if(item.getName().endsWith(".class")) {
				// Potential class found
				String className = packageName + "." + 
						item.getName().substring(0, item.getName().lastIndexOf("."));
				Class classFound = null;
				try {
					classFound = Class.forName(className);
				}
				catch(ClassNotFoundException e) {
					// Not really a class, resuming operation
					continue;
				}
				if(superClassNameFilter!=null) {
					if(className.equals(superClassNameFilter))
						continue;
					else {
						// Checking super class filter										
						if(superClassFilter.isAssignableFrom(classFound))
							foundClassesList.add(className);
					}
				}
				else
					foundClassesList.add(className);
			}
		}
	}
	
	/**
	 * Checks whether a package exists
	 * @param packageName (String) The package name
	 * @return (boolean)
	 */
	public static boolean packageExists(String packageName) {
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL packageUrl = classLoader.getResource(packageName.replace(".", "/"));
		return packageUrl!=null;
	}
}
/* ****************************************************************************************************************** */