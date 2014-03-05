package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class DerefExpression extends UnaryExpression {
	public DerefExpression(Expression e) {
		super(e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitDerefExpression(this, attr);
	}
}
