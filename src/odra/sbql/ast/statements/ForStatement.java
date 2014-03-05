package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;
import odra.transactions.ast.ITransactionCapableASTNode;

public final class ForStatement extends Statement {
	
	private Expression E1;
	private Expression E2;
	private Expression E3;

	private Statement S;

	public ForStatement(Expression e1, Expression e2, Expression e3, Statement s) {
		this.E1 = e1;
		this.E2 = e2;
		this.E3 = e3;
		this.S = s;

	}

	public void fixUpExpression() {
		while (E1.getParentExpression() != null) {
			E1 = E1.getParentExpression();
		}
		while (E2.getParentExpression() != null) {
			E2 = E2.getParentExpression();
		}
		while (E3.getParentExpression() != null) {
			E3 = E3.getParentExpression();
		}
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitForStatement(this, attr);
	}

	public Statement getStatement() {
		return this.S;
	}

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
		children.add(this.getStatement());
		return children;
	}

	/**
	 * @param e1 the e1 to set
	 */
	public void setInitExpression(Expression e1)
	{
	    E1 = e1;
	}

	/**
	 * @return the e1
	 */
	public Expression getInitExpression()
	{
	    return E1;
	}

	/**
	 * @param e2 the e2 to set
	 */
	public void setConditionalExpression(Expression e2)
	{
	    E2 = e2;
	}

	/**
	 * @return the e2
	 */
	public Expression getConditionalExpression()
	{
	    return E2;
	}

	/**
	 * @param e3 the e3 to set
	 */
	public void setIncrementExpression(Expression e3)
	{
	    E3 = e3;
	}

	/**
	 * @return the e3
	 */
	public Expression getIncrementExpression()
	{
	    return E3;
	}


}