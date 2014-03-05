package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.BooleanLiteral;

public class BooleanExpression extends Expression {
	private BooleanLiteral L;
	
	public BooleanExpression(BooleanLiteral l) {
		L = l;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitBooleanExpression(this, attr);
	}


	/**
	 * @return the l
	 */
	public BooleanLiteral getLiteral()
	{
	    return L;
	}

	
	
}
