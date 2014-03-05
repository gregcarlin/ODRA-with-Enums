package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ProcedureCallExpression extends Expression {
	private Expression E1;
	private Expression E2;
	
	public ProcedureCallExpression(Expression e1, Expression e2) {
		setProcedureSelectorExpression(e1);
		setArgumentsExpression(e2);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitProcedureCallExpression(this, attr);
	}
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (E1 == oldexpr)
			E1 = newexpr;
		else if (E2 == oldexpr)
			E2 = newexpr;
		else
			assert false : "The AST node does not have the specified subnode";
		
	}

	/**
	 * @param e2 the e2 to set
	 */
	public void setArgumentsExpression(Expression e2)
	{
	    E2 = e2;
	    e2.setParentExpression(this);
	}

	/**
	 * @return the e2
	 */
	public Expression getArgumentsExpression()
	{
	    return E2;
	}

	/**
	 * @return the e1
	 */
	public Expression getProcedureSelectorExpression() {
	    return E1;
	}

	/**
	 * @param e1 the e1 to set
	 */
	public void setProcedureSelectorExpression(Expression e1) {
	    E1 = e1;
	    e1.setParentExpression(this);
	}
}
