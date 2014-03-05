package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class InsertCopyExpression extends BinaryExpression {

    	private Name N;
	/**
	 * @param e1
	 * @param e2
	 */
	public InsertCopyExpression(Expression e1, Expression e2, Name n) {
		super(e1,e2);
		this.N = n;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitInsertCopyExpression(this, attr);
	}

	
	/**
	 * @return the n
	 */
	public Name name()
	{
	    return N;
	}
}
