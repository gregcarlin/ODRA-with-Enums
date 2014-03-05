package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class UniqueExpression extends UnaryExpression {
	
	private boolean uniqueref; // indicates uniqueref expression 
	
	public UniqueExpression(Expression e1, boolean uniqueref) {
		super(e1);
		this.uniqueref = uniqueref;
	}
	
	public boolean isUniqueref() {
		return uniqueref; 
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitUniqueExpression(this, attr);
	}
}
