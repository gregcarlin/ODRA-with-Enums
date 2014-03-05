package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * Represents one of the possible module members, i.e. a procedure. Associates
 * the proper {@link ProcedureDeclaration} with the currently constructed
 * module.
 * 
 * @see ProcedureDeclaration
 * 
 * @author raist, radamus, edek (comments)
 */
public class ProcedureFieldDeclaration extends SingleFieldDeclaration {
    private ProcedureDeclaration D;

    public ProcedureFieldDeclaration(ProcedureDeclaration p) {
	D = p;
    }

    /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
     */
    @Override
    public Declaration getDeclaration() {
	return getProcedureDeclaration();
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitProcedureFieldDeclaration(this, attr);
    }

    public String getName() {
	return D.getName();
    }

    /**
     * @param d
     *                the d to set
     */
    public void setProcedureDeclaration(ProcedureDeclaration d) {
	D = d;
    }

    /**
     * @return the d
     */
    public ProcedureDeclaration getProcedureDeclaration() {
	return D;
    }
    
}