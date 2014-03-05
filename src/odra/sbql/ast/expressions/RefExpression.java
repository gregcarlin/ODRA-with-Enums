package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class RefExpression extends UnaryExpression {

	/**
	 * @param e
	 */
	public RefExpression(Expression e) {
		super(e);
		// TODO Auto-generated constructor stub
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRefExpression(this, attr);
	}
}
