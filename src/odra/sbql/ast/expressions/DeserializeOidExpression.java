/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.results.compiletime.Signature;

/**
 * DeserializeOidExpression
 * @author Radek Adamus
 *@since 2008-06-09
 *last modified: 2008-06-09
 *@version 1.0
 */
public class DeserializeOidExpression extends BinaryExpression {

	/**
	 * @param e1
	 * @param e2
	 */
	public DeserializeOidExpression(Expression e1, Expression e2) {
		super(e1, e2);
	
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitDeserializeOidExpression(this, attr);
	}
		
	
}
