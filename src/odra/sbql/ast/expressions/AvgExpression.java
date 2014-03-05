package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class AvgExpression extends UnaryExpression {
	public AvgExpression(Expression e) {
		super(e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitAvgExpression(this, attr);
	}
}
