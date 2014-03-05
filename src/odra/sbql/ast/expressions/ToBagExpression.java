package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ToBagExpression extends UnaryExpression {
	public ToBagExpression(Expression e) {
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitToBagExpression(this, attr);
	}
}
