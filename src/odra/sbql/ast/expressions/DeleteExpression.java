package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class DeleteExpression extends Expression {
	private Expression E;

	/**
	 * @param e
	 */
	public DeleteExpression(Expression e) {
		setExpression(e);
	}
	
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (E == oldexpr)
			setExpression(newexpr);
		else
			assert false : "The AST node does not have the specified subnode";
		
		
	
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitDeleteExpression(this, attr);
	}


	/**
	 * @param e the e to set
	 */
	public void setExpression(Expression e)
	{
	    E = e;
	    e.setParentExpression(this);
	}


	/**
	 * @return the e
	 */
	public Expression getExpression()
	{
	    return E;
	}
	
	
}
