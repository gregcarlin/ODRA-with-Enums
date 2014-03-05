package odra.db.indices;

import odra.db.DatabaseException;

/**
 * If something goes wrong in indexing, we communicate it using this exception.
 * 
 * @author tkowals
 */
public class IndicesException extends DatabaseException {

	private final static long serialVersionUID = 20080513L;
	
	public IndicesException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
