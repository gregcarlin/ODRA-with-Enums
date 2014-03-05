/**
 * 
 */
package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;

/**
 * ThrowStatement
 * @author Radek Adamus
 *@since 2007-09-21
 *last modified: 2007-09-21
 *@version 1.0
 */
public class ThrowStatement extends ExpressionStatement {
    /**
     * @param e
     */
    public ThrowStatement(Expression e) {
	super(e);
    }
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitThrowStatement(this, attr);
}
    
    
    
}
