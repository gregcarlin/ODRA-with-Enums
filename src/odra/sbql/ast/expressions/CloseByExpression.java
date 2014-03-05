package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class CloseByExpression extends TransitiveClosureExpression {

	public CloseByExpression(Expression e1, Expression e2) {
		super(e1, e2);
		// TODO Auto-generated constructor stub
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCloseByExpression(this, attr);
	}
}
