package odra.transactions.store;

import odra.db.IDataStoreExtension;

public interface IDataStoreTransactionExtension extends IDataStoreExtension {

	void setEnabled(boolean enabled);
}