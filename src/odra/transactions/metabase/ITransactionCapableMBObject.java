package odra.transactions.metabase;

import odra.db.objects.meta.MBObject;
import odra.transactions.ITransactionCapable;

public interface ITransactionCapableMBObject extends ITransactionCapable {

	MBObject getMBObjectContainer();

	IMBTransactionCapabilities getMBTransactionCapabilities();
}