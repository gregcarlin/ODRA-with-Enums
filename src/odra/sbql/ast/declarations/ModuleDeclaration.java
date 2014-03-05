package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

/**
 * Represents declaration of a module in the abstract syntax tree
 * 
 * @author raist, comments: edek
 */

public class ModuleDeclaration extends Declaration {
    private Name N;

    private ModuleBody moduleBody;

    public ModuleDeclaration(Name n, ModuleBody b) {
	N = n;
	moduleBody = b;
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitModuleDeclaration(this, attr);
    }

    /**
     * @param d
     *                the d to set
     */
    public void setModuleBody(ModuleBody d) {
	moduleBody = d;
    }

    /**
     * @return the d
     */
    public ModuleBody getModuleBody() {
	return moduleBody;
    }

    /**
     * @param n
     *                the n to set
     */
    public void setName(String n) {
	N = new Name(n);
    }

    /**
     * @return the n
     */
    public String getName() {
	return N.value();
    }
}