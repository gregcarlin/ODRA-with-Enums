package odra.transactions;

import odra.exceptions.OdraRuntimeException;

public class TransactionRuntimeException extends OdraRuntimeException {

	private final static TransactionsAssemblyInfo assemblyInfo = TransactionsAssemblyInfo.getInstance();

	public TransactionRuntimeException(String message) {
		super(message);
	}

	public TransactionRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionRuntimeException(Class c, String key, String details) {
		super(assemblyInfo, c, key, details);
	}

	public TransactionRuntimeException(Class c, String key, String details, Throwable ex) {
		super(assemblyInfo, c, key, details, ex);
	}

	public TransactionRuntimeException(Class c, String key) {
		super(assemblyInfo, c, key);
	}

	public TransactionRuntimeException(Class c, String key, Throwable ex) {
		super(assemblyInfo, c, key, ex);
	}
}