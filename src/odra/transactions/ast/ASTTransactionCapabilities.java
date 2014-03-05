package odra.transactions.ast;

import odra.transactions.TransactionCapabilities;

public final class ASTTransactionCapabilities extends TransactionCapabilities implements IASTTransactionCapabilities {

	private ASTTransactionCapabilities() {
	}

	public static IASTTransactionCapabilities getInstance() {
		return new ASTTransactionCapabilities();
	}
}