package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class SequentialExpression extends Expression {
	
	private Expression E1;
	private Expression E2;
	
	public SequentialExpression(Expression e1, Expression e2) {
		E1 = e1;
		e1.P = this;
		E2 = e2;
		e2.P = this;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitSequentialExpression(this, attr);
	}
	
	public Expression[] flatten(){
		Expression[] e1f = E1.flatten();
		Expression[] e2f = E2.flatten();
		Expression[] seq = new Expression[e1f.length + e2f.length];
		int i;
		for( i = 0; i< e1f.length; i++) 
			seq[i] = e1f[i];
		for(int j = 0; j< e2f.length; j++) 
			seq[i + j] = e2f[j];
		return seq;
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
	public void setFirstExpression(Expression e1)
	{
	    E1 = e1;
	}

	/**
	 * @return the e1
	 */
	public Expression getFirstExpression()
	{
	    return E1;
	}

	/**
	 * @param e2 the e2 to set
	 */
	public final void setSecondExpression(Expression e2)
	{
	    E2 = e2;
	}

	/**
	 * @return the e2
	 */
	public final Expression getSecondExpression()
	{
	    return E2;
	}
}
