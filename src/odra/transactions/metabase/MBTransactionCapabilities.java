package odra.transactions.metabase;

import odra.transactions.TransactionCapabilities;
import odra.transactions.ast.IASTTransactionCapabilities;

public final class MBTransactionCapabilities extends TransactionCapabilities implements IMBTransactionCapabilities {

	private MBTransactionCapabilities(IASTTransactionCapabilities capsASTTransaction) {
	}

	public static IMBTransactionCapabilities getInstance(IASTTransactionCapabilities capsASTTransaction) {
		IMBTransactionCapabilities capsMBTransaction = null;
		if (capsASTTransaction != null) {
			capsMBTransaction = new MBTransactionCapabilities(capsASTTransaction);
		}
		return capsMBTransaction;
	}
}