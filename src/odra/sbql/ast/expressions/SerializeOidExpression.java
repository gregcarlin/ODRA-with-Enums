/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * OidExpression - converts reference to OID representation
 * 
 * @author Radek Adamus
 *@since 2008-06-04
 *last modified: 2008-06-04
 *@version 1.0
 */
public class SerializeOidExpression extends UnaryExpression {

	/**
	 * @param e
	 */
	public SerializeOidExpression(Expression e) {
		super(e);
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitSerializeOidExpression(this, attr);
	}
	
	
}
