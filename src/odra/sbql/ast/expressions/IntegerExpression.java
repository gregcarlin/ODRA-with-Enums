package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.IntegerLiteral;

public class IntegerExpression extends Expression {
	private IntegerLiteral L;
	
	public IntegerExpression(IntegerLiteral l) {
		L = l;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitIntegerExpression(this, attr);
	}


	/**
	 * @return the l
	 */
	public IntegerLiteral getLiteral()
	{
	    return L;
	}
}
