/**
 * 
 */
package odra.sbql.ast.declarations;

import odra.sbql.ast.terminals.Name;

/**
 * NamedSingleImportDeclaration
 * @author Radek Adamus
 *@since 2008-05-09
 *last modified: 2008-05-09
 *@version 1.0
 */
public class NamedSingleImportDeclaration extends SingleImportDeclaration {

    private Name alias;
    /**
     * @param n
     */
    public NamedSingleImportDeclaration(CompoundName n, Name alias) {
	super(n);
	this.alias = alias;
    }
    /**
     * @return the alias
     */
    public String getAlias() {
        return alias.value();
    }
    
    public SingleImportDeclaration[] flattenImports() {
	return new NamedSingleImportDeclaration[] { this };
}
}
