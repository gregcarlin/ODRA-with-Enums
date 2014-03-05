package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class ExternalNameExpression extends NameExpression {
	

	public ExternalNameExpression(Name n) {
	    super(n);
		
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitExternalNameExpression(this, attr);
	}
}
