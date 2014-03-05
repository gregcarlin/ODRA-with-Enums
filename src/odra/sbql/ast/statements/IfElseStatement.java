package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;
import odra.transactions.ast.ITransactionCapableASTNode;

public final class IfElseStatement extends Statement {
	private Statement S2;

	private Statement S1;

	private Expression E;

	public IfElseStatement(Expression e, Statement s1, Statement s2) {
		E = e;
		S1 = s1;
		S2 = s2;
		
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitIfElseStatement(this, attr);
	}

	/**
	 * during compilation statement expression can be modified through adding new parent expressions (e.g. dereference)
	 * we need to re-clip the statement expresion to the root
	 */
	public void fixUpExpression() {
		while (E.getParentExpression() != null) {
			E = E.getParentExpression();
		}
	}

	public Statement getIfStatement() {
		return this.S1;
	}

	public Statement getElseStatement() {
		return this.S2;
	}

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
		children.add(this.getIfStatement());
		children.add(this.getElseStatement());
		return children;
	}

	/**
	 * @param s2 the s2 to set
	 */
	public void setElseStatement(Statement s2)
	{
	    S2 = s2;
	}

	/**
	 * @param e the e to set
	 */
	public void setExpression(Expression e) {
	    E = e;
	}

	/**
	 * @return the e
	 */
	public Expression getExpression() {
	    return E;
	}

}