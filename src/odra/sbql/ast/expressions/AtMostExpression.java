/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * AtMostExpression
 * @author Radek Adamus
 *@since 2007-08-09
 *last modified: 2007-08-09
 *@version 1.0
 */
public class AtMostExpression extends UnaryExpression {

    private int maxCardinality;
    

    /**
     * @param e
     */
    public AtMostExpression(Expression e, int maxCardinality) {
	super(e);
	this.maxCardinality = maxCardinality;

    }
    
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitAtMostExpression(this, attr);
}
    /**
     * @return the maxCardinality
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }
}
