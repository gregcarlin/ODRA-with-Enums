/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * SessionVariableFieldDeclaration
 * @author Radek Adamus
 *@since 2008-04-29
 *last modified: 2008-04-29
 *@version 1.0
 */
public class SessionVariableFieldDeclaration extends SingleFieldDeclaration {
    private VariableDeclaration variableDeclaration;
    
    /**
     * @param read
     */
    public SessionVariableFieldDeclaration(VariableDeclaration variableDeclaration) {
	this.variableDeclaration = variableDeclaration;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
     */
    @Override
    public Declaration getDeclaration() {
	return variableDeclaration;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getName()
     */
    @Override
    public String getName() {
	
	return variableDeclaration.getName();
    }
    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitSessionVariableFieldDeclaration(this, attr);
    }

    /**
     * @return
     */
    public VariableDeclaration getVariableDeclaration() {	
	return variableDeclaration;
    }
}
