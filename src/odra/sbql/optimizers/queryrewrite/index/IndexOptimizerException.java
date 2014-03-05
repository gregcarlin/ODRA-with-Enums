package odra.sbql.optimizers.queryrewrite.index;

import odra.sbql.SBQLException;

/**
 * If something goes wrong in index optimization routines, 
 * we communicate it using this exception.
 * 
 * @author tkowals
 */

public class IndexOptimizerException extends SBQLException {

	private final static long serialVersionUID = 20070513L;
	
	public IndexOptimizerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param ex
	 */
	public IndexOptimizerException(Throwable ex) {
	    super(ex);
	    // TODO Auto-generated constructor stub
	}
	
}
