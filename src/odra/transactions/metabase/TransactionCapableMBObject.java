package odra.transactions.metabase;

import odra.db.objects.meta.MBObject;
import odra.system.config.ConfigDebug;
import odra.transactions.impl.TransactionCapableImpl;

public final class TransactionCapableMBObject extends TransactionCapableImpl implements ITransactionCapableMBObject {

	private final IMBTransactionCapabilities capsMBTransaction;

	private final MBObject mbObjectContainer;

	private TransactionCapableMBObject(MBObject mbObject, IMBTransactionCapabilities capsMBTransaction) {
		super(capsMBTransaction);
		this.capsMBTransaction = capsMBTransaction;
		this.mbObjectContainer = mbObject;
	}

	public static ITransactionCapableMBObject getInstance(MBObject mbObject, IMBTransactionCapabilities capsMBTransaction) {
		ITransactionCapableMBObject retVal = null;
		if (capsMBTransaction != null) {
			retVal = new TransactionCapableMBObject(mbObject, capsMBTransaction);
		}
		return retVal;
	}

	public MBObject getMBObjectContainer() {
		return this.mbObjectContainer;
	}

	public boolean isTransactionCapable() {
		if (ConfigDebug.ASSERTS) {
			assert this.mbObjectContainer != null : "MBObject container has not been set";
		}
		return this.mbObjectContainer.isTransactionCapable();
	}

	public IMBTransactionCapabilities getMBTransactionCapabilities() {
		return this.capsMBTransaction;
	}
}