package odra.transactions.ast;

import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.transactions.ITransactionCapable;

public interface ITransactionCapableASTNode extends ITransactionCapable {

	ASTNode getASTNodeContainer();

	IASTTransactionCapabilities getASTTransactionCapabilities();

	boolean hasTransactionCapableParentASTNode();

	boolean isTransactionCapableMainASTNode();

	ITransactionCapableASTNode getTransactionCapableParentASTNode();

	void setTransactionCapableParentASTNode(ITransactionCapableASTNode parentNode);

	List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes();

	Object accept(ASTVisitor visitor, Object attribute) throws SBQLException;
}