package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class StructExpression extends UnaryExpression {
	
	public StructExpression(Expression e) {
		super(e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitStructExpression(this, attr);
	}	
}