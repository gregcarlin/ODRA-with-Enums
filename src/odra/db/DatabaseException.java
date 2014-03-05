package odra.db;

import odra.exceptions.OdraCoreException;

/**
 * If something goes wrong in the data store, we communicate it using this exception.
 * 
 * @author raist
 * 
 * edek: some slight improvements that allow to obtain the chain of exceptions
 */
public class DatabaseException extends OdraCoreException {

	// TODO: MOST PROBABLY THERE SHOULD BE DatabaseException AND StoreException

	private final static long serialVersionUID = 20062304L;

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(Class c, String key, String details) {
		super(c, key, details);
	}

	public DatabaseException(Class c, String key, String details, Throwable ex) {
		super(c, key, details, ex);
	}

	public DatabaseException(Class c, String key) {
		super(c, key);
	}

	public DatabaseException(Class c, String key, Throwable ex) {
		super(c, key, ex);
	}
}