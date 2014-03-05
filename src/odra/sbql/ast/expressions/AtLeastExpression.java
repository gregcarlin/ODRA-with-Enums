/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * AtLeastExpression
 * @author Radek Adamus
 *@since 2008-03-26
 *last modified: 2008-03-26
 *@version 1.0
 */
public class AtLeastExpression extends UnaryExpression {
    private int minCardinality;
    /**
     * @param e
     */
    public AtLeastExpression(Expression e, int minCardinality) {
	super(e);
	this.minCardinality = minCardinality;
    }
    
    /**
     * @return the minCardinality
     */
    public int getMinCardinality() {
        return minCardinality;
    }
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitAtLeastExpression(this, attr);
    }
}
