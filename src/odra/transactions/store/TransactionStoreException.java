package odra.transactions.store;

import odra.db.DatabaseException;
import odra.exceptions.OdraException;
import odra.transactions.TransactionException;

public class TransactionStoreException extends TransactionException {

	public TransactionStoreException(String message) {
		super(message);
	}

	public TransactionStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionStoreException(Class c, String key, String details) {
		super(c, key, details);
	}

	public TransactionStoreException(Class c, String key, String details, Throwable ex) {
		super(c, key, details, ex);
	}

	public TransactionStoreException(Class c, String key) {
		super(c, key);
	}

	public TransactionStoreException(Class c, String key, Throwable ex) {
		super(c, key, ex);
	}
}