package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class CastExpression extends BinaryExpression {
	
	public CastExpression(Expression e1, Expression e2) {
		super(e1,e2);
		
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCastExpression(this, attr);
	}
}
