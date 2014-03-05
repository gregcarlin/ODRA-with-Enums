package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ToBooleanExpression extends UnaryExpression {
	public ToBooleanExpression(Expression e) {
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitToBooleanExpression(this, attr);
	}
}
