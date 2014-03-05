package odra.transactions;

import odra.exceptions.OdraException;

public class TransactionException extends OdraException {

	protected final static TransactionsAssemblyInfo assemblyInfo = TransactionsAssemblyInfo.getInstance();

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(Class c, String key, String details) {
		super(assemblyInfo, c, key, details);
	}

	public TransactionException(Class c, String key, String details, Throwable ex) {
		super(assemblyInfo, c, key, details, ex);
	}

	public TransactionException(Class c, String key) {
		super(assemblyInfo, c, key);
	}

	public TransactionException(Class c, String key, Throwable ex) {
		super(assemblyInfo, c, key, ex);
	}
}