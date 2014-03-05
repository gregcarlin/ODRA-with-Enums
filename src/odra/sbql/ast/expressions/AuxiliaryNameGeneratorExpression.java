/**
 * 
 */
package odra.sbql.ast.expressions;

import odra.sbql.ast.terminals.Name;

/**
 * AuxiliaryNameGeneratorExpression
 * @author Radek Adamus
 *@since 2007-12-07
 *last modified: 2007-12-07
 *@version 1.0
 */
public abstract class AuxiliaryNameGeneratorExpression extends UnaryExpression {
    protected Name N;
    /**
     * @param e
     */
    protected AuxiliaryNameGeneratorExpression(Expression e, Name n) {
	super(e);
	this.N = n;
	// TODO Auto-generated constructor stub
    }

    /**
	 * @return the n
	 */
	public Name name()
	{
	    return N;
	}
}
