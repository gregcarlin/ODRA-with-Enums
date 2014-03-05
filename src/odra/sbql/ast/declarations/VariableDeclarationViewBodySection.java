/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.ast.ParserException;

/**
 * VariableDeclarationViewBodySection
 * @author Radek Adamus
 *@since 2007-05-05
 *last modified: 2007-05-05
 *@version 1.0
 */
public class VariableDeclarationViewBodySection extends ViewBodySection {

    private VariableDeclaration d;
    /**
     * 
     */
    public VariableDeclarationViewBodySection(VariableDeclaration d) {
	this.d = d;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.ViewBodySection#putSelfInSection(odra.sbql.ast.declarations.ViewBody)
     */
    @Override
    public void putSelfInSection(ViewBody vb) throws ParserException {
	vb.addVariableDeclaration(this);

    }


    /**
     * @return the d
     */
    public VariableDeclaration getDeclaration() {
	return d;
    }

}
