package odra.transactions.store;

import odra.store.sbastore.IObjectManagerExtension;

public interface IObjectManagerTransactionExtension extends IObjectManagerExtension {

	void setEnabled(boolean enabled);
}