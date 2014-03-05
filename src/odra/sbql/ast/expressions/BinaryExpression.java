package odra.sbql.ast.expressions;


public abstract class BinaryExpression extends Expression {
	
	private Expression E1;
	private Expression E2;
	public BinaryExpression(Expression e1, Expression e2) {
		setLeftExpression(e1);
		setRightExpression(e2);
	}
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (getLeftExpression() == oldexpr)
			setLeftExpression(newexpr);
		else if (getRightExpression() == oldexpr)
			setRightExpression(newexpr);
		else
			assert false : "The AST node does not have the specified subnode";
		
	}

	/**
	 * @param e1 the e1 to set
	 */
	public void setLeftExpression(Expression e1)
	{
	    E1 = e1;
	    e1.setParentExpression(this);
	}

	/**
	 * @return the e1
	 */
	public Expression getLeftExpression()
	{
	    return E1;
	}

	/**
	 * @param e2 the e2 to set
	 */
	public void setRightExpression(Expression e2)
	{
	    E2 = e2;
	    e2.setParentExpression(this);
	}

	/**
	 * @return the e2
	 */
	public Expression getRightExpression()
	{
	    return E2;
	}

	
	
}
