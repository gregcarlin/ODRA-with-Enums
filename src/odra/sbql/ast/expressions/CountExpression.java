package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class CountExpression extends UnaryExpression {
	public CountExpression(Expression e) {
		super(e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCountExpression(this, attr);
	}
}
