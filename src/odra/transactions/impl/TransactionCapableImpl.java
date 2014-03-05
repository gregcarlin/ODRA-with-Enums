package odra.transactions.impl;

import odra.transactions.ITransactionCapabilities;
import odra.transactions.ITransactionCapable;

public abstract class TransactionCapableImpl implements ITransactionCapable {

	private final ITransactionCapabilities capsTransaction;

	protected TransactionCapableImpl(ITransactionCapabilities capsTransaction) {
		this.capsTransaction = capsTransaction;
	}

	public final ITransactionCapabilities getTransactionCapabilities() {
		return this.capsTransaction;
	}
}