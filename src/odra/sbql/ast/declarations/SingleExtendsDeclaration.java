/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.ast.terminals.Name;

/**
 * SingleExtendsDeclaration
 * @author Radek Adamus
 *@since 2008-04-28
 *last modified: 2008-04-28
 *@version 1.0
 */
public class SingleExtendsDeclaration extends ExtendsDeclaration {
    protected Name N;

    /**
     * @param n
     */
    public SingleExtendsDeclaration(Name n) {
	this.N = n;
	// TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.ExtendsDeclaration#flattenArguments()
     */
    @Override
    public SingleExtendsDeclaration[] flattenExtends() {
	return new SingleExtendsDeclaration[] {this};	
    }
    
}
