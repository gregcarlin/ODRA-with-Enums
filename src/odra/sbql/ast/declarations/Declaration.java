package odra.sbql.ast.declarations;

import odra.sbql.ast.ASTNode;
import odra.transactions.ast.IASTTransactionCapabilities;

/**
 * Base class for all AST nodes being declarations.
 * 
 * @author raist
 */
public abstract class Declaration extends ASTNode {

	protected Declaration() {
		super();
	}

	protected Declaration(IASTTransactionCapabilities capsASTTransaction) {
		super(capsASTTransaction);
	}
}