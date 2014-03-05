package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ExternalProcedureCallExpression extends Expression {
	private Expression E1;
	private Expression E2;
	
	public ExternalProcedureCallExpression(Expression e1, Expression e2) {
		E1 = e1;
		e1.P = this;
		E2 = e2;
		e2.P = this;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitExternalProcedureCallExpression(this, attr);
	}
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (E1 == oldexpr)
			E1 = newexpr;
		else if (E2 == oldexpr)
			E2 = newexpr;
		else
			assert false : "The AST node does not have the specified subnode";
		
		newexpr.P = this;
	}

	/**
	 * @param e1 the e1 to set
	 */
	public void setLeftExpression(Expression e1) {
		E1 = e1;
	}

	/**
	 * @return the e1
	 */
	public Expression getLeftExpression() {
		return E1;
	}

	/**
	 * @param e2 the e2 to set
	 */
	public void setRightExpression(Expression e2) {
		E2 = e2;
	}

	/**
	 * @return the e2
	 */
	public Expression getRightExpression() {
		return E2;
	}
}
