package odra.transactions.ast;

import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.system.config.ConfigDebug;
import odra.transactions.TransactionRuntimeException;
import odra.transactions.impl.TransactionCapableImpl;

public final class TransactionCapableASTNode extends TransactionCapableImpl implements ITransactionCapableASTNode {

	private final IASTTransactionCapabilities capsASTTransaction;

	private final ASTNode astNodeContainer;

	private TransactionCapableASTNode(ASTNode astNode, IASTTransactionCapabilities capsASTTransaction) {
		super(capsASTTransaction);
		this.capsASTTransaction = capsASTTransaction;
		this.astNodeContainer = astNode;
	}

	public static ITransactionCapableASTNode getInstance(ASTNode astNode, IASTTransactionCapabilities capsASTTransaction) {
		if (ConfigDebug.ASSERTS) {
			assert astNode != null : ASTNode.class + " container may not be null";
		}
		ITransactionCapableASTNode retVal = null;
		if (capsASTTransaction != null) {
			retVal = new TransactionCapableASTNode(astNode, capsASTTransaction);
		}
		return retVal;
	}

	public ASTNode getASTNodeContainer() {
		return this.astNodeContainer;
	}

	public boolean isTransactionCapable() {
		this.checkASTNodeContainer();
		return this.astNodeContainer.isTransactionCapable();
	}

	public IASTTransactionCapabilities getASTTransactionCapabilities() {
		IASTTransactionCapabilities capsASTTransaction = null;
		if (this.hasTransactionCapableParentASTNode()) {
			ITransactionCapableASTNode parent = this.getTransactionCapableParentASTNode();
			capsASTTransaction = parent.getASTTransactionCapabilities();
		} else {
			capsASTTransaction = this.capsASTTransaction;
		}
		return capsASTTransaction;
	}

	public boolean hasTransactionCapableParentASTNode() {
		this.checkASTNodeContainer();
		return this.astNodeContainer.hasTransactionCapableParentASTNode();
	}

	public boolean isTransactionCapableMainASTNode() {
		this.checkASTNodeContainer();
		return this.astNodeContainer.isTransactionCapableMainASTNode();
	}

	public ITransactionCapableASTNode getTransactionCapableParentASTNode() {
		this.checkASTNodeContainer();
		return this.astNodeContainer.getTransactionCapableParentASTNode();
	}

	private final static String ERROR_SET_TRANSACTION_CAPABLE_PARENT_ASTNODE = "ERROR_SET_TRANSACTION_CAPABLE_PARENT_ASTNODE";

	private final static String FEATURE_NOT_IMPLEMENTED = "feature not implemented --- it should be delivered by a proper implementation of "
				+ ITransactionCapableASTNode.class + " which encapsulates the present one";

	public void setTransactionCapableParentASTNode(ITransactionCapableASTNode parentNode) {
		throw new TransactionRuntimeException(TransactionCapableASTNode.class,
					ERROR_SET_TRANSACTION_CAPABLE_PARENT_ASTNODE, FEATURE_NOT_IMPLEMENTED);
	}

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		this.checkASTNodeContainer();
		return this.astNodeContainer.getTransactionCapableChildrenASTNodes();
	}

	public Object accept(ASTVisitor visitor, Object attribute) throws SBQLException {
		this.checkASTNodeContainer();
		return this.astNodeContainer.accept(visitor, attribute);
	}

	private void checkASTNodeContainer() {
		if (ConfigDebug.ASSERTS) {
			assert this.astNodeContainer != null : ASTNode.class + " container has not been set";
		}
	}
}