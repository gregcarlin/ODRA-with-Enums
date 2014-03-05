package odra.transactions.store;

import odra.transactions.TransactionRuntimeException;

public class TransactionStoreRuntimeException extends TransactionRuntimeException {

	public TransactionStoreRuntimeException(String message) {
		super(message);
	}

	public TransactionStoreRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionStoreRuntimeException(Class c, String key, String details) {
		super(c, key, details);
	}

	public TransactionStoreRuntimeException(Class c, String key, String details, Throwable ex) {
		super(c, key, details, ex);
	}

	public TransactionStoreRuntimeException(Class c, String key) {
		super(c, key);
	}

	public TransactionStoreRuntimeException(Class c, String key, Throwable ex) {
		super(c, key, ex);
	}
}