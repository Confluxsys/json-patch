/**
 * 
 */
package com.github.fge.jsonpatch;

/**
 * @author Ritesh
 *
 */
public class JsonDiffConstants {
	
	//Supported Operation of the Library
	public static final String ADD = "add";
	public static final String REMOVE = "remove";
	public static final String REPLACE = "replace";
	
	
	//Original output format as per operation
	public static final String OPERATION ="op";
	public static final String PATH = "path";
	public static final String VALUE = "value";
	
	
	//Custom Addition to the original Library
	public static final String ORIGINAL_VALUE = "original_value";
	public static final String VALUE_LOCATOR = "value_locator";
	public static final String OLD_VALUE = "oldValue";
	public static final String NEW_VALUE = "newValue";
	
	

}
