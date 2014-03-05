package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.StringLiteral;

public class StringExpression extends Expression {
	private StringLiteral L;
	
	public StringExpression(StringLiteral l) {
		L =l;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitStringExpression(this, attr);
	}

	

	/**
	 * @return the l
	 */
	public StringLiteral getLiteral()
	{
	    return L;
	}
}
