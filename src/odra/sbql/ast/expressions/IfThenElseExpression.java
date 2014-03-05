package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class IfThenElseExpression extends Expression {
	private Expression E1;
	private Expression E2;
	private Expression E3;
	
	public IfThenElseExpression(Expression E1, Expression E2, Expression E3){
		setConditionExpression(E1);
		setThenExpression(E2);
		setElseExpression(E3);		
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitIfThenElseExpression(this, attr);
	}
	
	
	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		if (E1 == oldexpr)
			setConditionExpression(newexpr);			
		else if (E2 == oldexpr)
			setThenExpression(newexpr);			
		else if (E3 == oldexpr)
			setElseExpression(newexpr);
		else
			assert false : "The AST node does not have the specified subnode";		
	}

	/**
	 * @param e1 the e1 to set
	 */
	public void setConditionExpression(Expression e1) {
	    E1 = e1;
	    e1.setParentExpression(this);
	}

	/**
	 * @return the e1
	 */
	public Expression getConditionExpression() {
	    return E1;
	}

	/**
	 * @param e2 the e2 to set
	 */
	public void setThenExpression(Expression e2) {
	    E2 = e2;
	    e2.setParentExpression(this);
	}

	/**
	 * @return the e2
	 */
	public Expression getThenExpression() {
	    return E2;
	}

	/**
	 * @param e3 the e3 to set
	 */
	public void setElseExpression(Expression e3) {
	    E3 = e3;
	    e3.setParentExpression(this);
	}

	/**
	 * @return the e3
	 */
	public Expression getElseExpression() {
	    return E3;
	}
	
}
