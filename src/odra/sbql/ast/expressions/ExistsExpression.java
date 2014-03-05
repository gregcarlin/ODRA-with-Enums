package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ExistsExpression extends UnaryExpression {
	public ExistsExpression(Expression e1) {
		super(e1);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitExistsExpression(this, attr);
	}
}
