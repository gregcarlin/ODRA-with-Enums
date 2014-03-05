/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.terminals.Name;

/**
 * ReverseVariableDeclaration
 * @author Radek Adamus
 *@since 2007-08-06
 *last modified: 2007-08-06
 *@version 1.0
 */
public class ReverseVariableDeclaration extends VariableDeclaration {
    private Name reverseName;
    /**
     * @param n1
     * @param d1
     * @param d2
     */
    public ReverseVariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, Name n2) {
	super(n1, d1, d2);
	reverseName = n2;
    }

    /**
     * @param n1
     * @param d1
     * @param d2
     * @param reflevel
     */
    public ReverseVariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, int reflevel , Name n2) {
	super(n1, d1, d2, reflevel);
	reverseName = n2;
	
    }

    /**
     * @param n1
     * @param d1
     * @param d2
     * @param e
     */
    public ReverseVariableDeclaration(Name n1, TypeDeclaration d1,
	    CardinalityDeclaration d2, Expression e, Name n2) {
	super(n1, d1, d2, e);
	reverseName = n2;
    }

    
    /**
     * @param vdec
     * @param reverseName2
     */
    public ReverseVariableDeclaration(VariableDeclaration varDecl,
	    Name reverseName) {
	super(varDecl.N, varDecl.D1, varDecl.D2, varDecl.E, varDecl.reflevel);
	this.reverseName = reverseName;
    }

    /**
     * @return the rN
     */
    public String getReverseName() {
	return reverseName.value();
    }

}
