/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

/**
 * DbLinkDeclaration
 * @author Radek Adamus
 *@since 2007-11-17
 *last modified: 2007-11-17
 *@version 1.0
 */
public class ExternalSchemaDefDeclaration extends Declaration {
    private Name n;
    private FieldDeclaration f;

    /**
     * @param n
     */
    public ExternalSchemaDefDeclaration(Name n, FieldDeclaration f) {
    	this.n = n;
    	this.f = f;
    }
    
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
    	return vis.visitExternalSchemaDefDeclaration(this, attr);
    }

    /**
     * @return the link name
     */
    public String getName() {
        return n.value();
    }

	public FieldDeclaration getFieldDeclaration() {
		return f;
	}
    
}
