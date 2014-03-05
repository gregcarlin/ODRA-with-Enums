package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class MaxExpression extends UnaryExpression {
	public MaxExpression(Expression e) {
		super(e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitMaxExpression(this, attr);
	}
}
