package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ToStringExpression extends UnaryExpression {
	public ToStringExpression(Expression e) {
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitToStringExpression(this, attr);
	}
}
