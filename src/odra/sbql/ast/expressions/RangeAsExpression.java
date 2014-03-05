/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

/**
 * RangeOfExpression
 * @author Radek Adamus
 *@since 2008-05-13
 *last modified: 2008-05-13
 *@version 1.0
 */
public class RangeAsExpression extends UnaryExpression {
	private Name N;
	/**
	 * @param e
	 */
	public RangeAsExpression(Expression e, Name n) {
		super(e);
		this.N = n;
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRangeAsExpression(this, attr);
	}
	
	/**
	 * @return the name
	 */
	public Name name()
	{
	    return N;
	}
}
