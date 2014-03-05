package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class LazyFailureExpression extends UnaryExpression {
	
	public LazyFailureExpression(Expression e) {
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitLazyFailureExpression(this, attr);
	}
}
