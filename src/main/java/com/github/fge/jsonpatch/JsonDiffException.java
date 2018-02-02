package com.github.fge.jsonpatch;

/**
 * @author Ritesh
 *
 */
public final class JsonDiffException extends JsonPatchException{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JsonDiffException(final String message)
	    {
	        super(message);
	    }

	    public JsonDiffException(final String message, final Throwable cause)
	    {
	        super(message, cause);
	    }
}
