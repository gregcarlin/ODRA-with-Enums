package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class AsExpression extends AuxiliaryNameGeneratorExpression{
	
	
	public AsExpression(Expression e, Name n) {
		super(e,n);
		
		
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitAsExpression(this, attr);
	}

		
}
