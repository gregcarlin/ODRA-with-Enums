/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;

/**
 * VirtualObjectsDeclarationViewBodySection
 * @author Radek Adamus
 *@since 2007-11-15
 *last modified: 2007-11-15
 *@version 1.0
 */
public class VirtualObjectsDeclarationViewBodySection extends ViewBodySection {
    private VariableDeclaration virtualObject;
    
    
    /**
     * @param virtualObject
     */
    public VirtualObjectsDeclarationViewBodySection(
	    VariableDeclaration virtualObject) {
	this.virtualObject = virtualObject;
    }
    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.ViewBodySection#putSelfInSection(odra.sbql.ast.declarations.ViewBody)
     */
    @Override
    public void putSelfInSection(ViewBody vb) throws ParserException {
	vb.addVirtualObjectDeclaration(this);

    }
    /**
     * @return the virtualObject
     */
    VariableDeclaration getVirtualObjectDeclaration() {
        return virtualObject;
    }

}
