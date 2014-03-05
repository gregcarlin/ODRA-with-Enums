/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

/**
 * RenameExpression
 * @author Radek Adamus
 *@since 2008-06-30
 *last modified: 2008-06-30
 *@version 1.0
 */
public class RenameExpression extends UnaryExpression {
	private Name N;
	/**
	 * @param e - param expression
	 */
	public RenameExpression(Expression e, Name n) {
		super(e);
		this.N = n;
	}
	public Name name(){
		return N;
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRenameExpression(this, attr);
	}
}
