package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class CreateLocalExpression extends CreateExpression {

	/**
	 * @param e
	 */
	public CreateLocalExpression(Name n, Expression e) {
		super(n,e);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCreateLocalExpression(this, attr);
	}
}
