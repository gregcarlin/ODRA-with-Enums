package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class EmptyExpression extends Expression {
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitEmptyExpression(this, attr);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.expressions.Expression#flatten()
	 */
	@Override
	public Expression[] flatten() {
		// TODO Auto-generated method stub
		return new Expression[0];
	}
}
