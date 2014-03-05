package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ToSingleExpression extends UnaryExpression {
	public ToSingleExpression(Expression e) {
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitToSingleExpression(this, attr);
	}
}
