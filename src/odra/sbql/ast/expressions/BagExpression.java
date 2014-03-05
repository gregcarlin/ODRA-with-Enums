package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class BagExpression extends UnaryExpression {
	
	
	public BagExpression(Expression e) {
		super(e);
		
		
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitBagExpression(this, attr);
	}

	

		
}
