package odra.transactions.store;

import odra.store.sbastore.AbstractObjectManagerExtension;

public final class ObjectManagerTransactionExtension extends AbstractObjectManagerExtension implements
			IObjectManagerTransactionExtension {

	private ObjectManagerTransactionExtension() {
		super(ExtensionType.TRANSACTIONS);
	}

	public static IObjectManagerTransactionExtension getInstance() {
		return new ObjectManagerTransactionExtension();
	}

	public void setEnabled(boolean enabled) {
			
	}
}