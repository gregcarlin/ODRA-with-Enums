package odra.transactions.store;

import odra.store.io.IHeapExtension;

public interface IHeapTransactionExtension extends IHeapExtension {

	void commitChanges() throws TransactionStoreException;

	boolean isTransactionCapable();

	void setEnabled(boolean enabled);
}