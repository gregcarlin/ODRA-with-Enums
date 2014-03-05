package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ProcedureHeaderFieldDeclaration extends SingleFieldDeclaration {
    
    private ProcedureHeaderDeclaration D;
   /* (non-Javadoc)
     * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
     */
    @Override
    public Declaration getDeclaration() {
	return getProcedureHeaderDeclaration();
    }

    

   /**
     * @return the d
     */
    public ProcedureHeaderDeclaration getProcedureHeaderDeclaration() {
        return D;
    }



    /**
     * @param d the d to set
     */
    public void setProcedureHeader(ProcedureHeaderDeclaration d) {
        D = d;
    }



public ProcedureHeaderFieldDeclaration(ProcedureHeaderDeclaration p) {
      D = p;
   }

   public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
       	return vis.visitProcedureHeaderFieldDeclaration(this, attr);	   
   }

   public String getName() {
      return D.getName();
   }
}
