package odra.transactions.store;

import odra.db.AbstractDataStoreExtension;

public final class DataStoreTransactionExtension extends AbstractDataStoreExtension implements IDataStoreTransactionExtension {

	private DataStoreTransactionExtension() {
		super(ExtensionType.TRANSACTIONS);
	}
	
	public static IDataStoreTransactionExtension getInstance() {
		return new DataStoreTransactionExtension();
	}
	
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
	}

}