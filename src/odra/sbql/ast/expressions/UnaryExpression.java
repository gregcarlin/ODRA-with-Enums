package odra.sbql.ast.expressions;


public abstract class UnaryExpression extends Expression {
	private Expression E;

	public UnaryExpression(Expression e) {
		setExpression(e);
	}
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (E == oldexpr)
			setExpression(newexpr);
		else
			assert false : "The AST node does not have the specified subnode";
		
	}


	/**
	 * @return the e
	 */
	public Expression getExpression() {
	    return E;
	}

	/**
	 * @return the e
	 */
	public void setExpression(Expression e) {
	    this.E = e;
	    e.setParentExpression(this);
	}

	
}
