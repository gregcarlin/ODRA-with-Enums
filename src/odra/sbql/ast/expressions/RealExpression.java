package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.RealLiteral;

public class RealExpression extends Expression {
	private RealLiteral L;
	
	public RealExpression(RealLiteral l) {
		L = l;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRealExpression(this, attr);
	}
	

	/**
	 * @return the l
	 */
	public RealLiteral getLiteral()
	{
	    return L;
	}
}
