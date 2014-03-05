/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * DbLinkFieldDeclaration
 * @author Radek Adamus
 *@since 2007-11-17
 *last modified: 2007-11-17
 *@version 1.0
 */
public class ExternalSchemaDefFieldDeclaration extends SingleFieldDeclaration {
    private ExternalSchemaDefDeclaration D;
    
    /**
     * @param d
     */
    public ExternalSchemaDefFieldDeclaration(ExternalSchemaDefDeclaration d) {
	D = d;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getName()
     */
    @Override
    public String getName() {
	return D.getName();
	
    }
    
    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
     */
    @Override
    public Declaration getDeclaration() {
	return getLinkDeclaration();
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitExternalSchemaDefFieldDeclaration(this, attr);
}

    /**
     * @return the db link declaration
     */
    public ExternalSchemaDefDeclaration getLinkDeclaration() {
        return D;
    }

}
