package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

/**
 * Auxilliary class representing the header of a procedure
 * 
 * @author raist
 */
public class ProcedureHeaderDeclaration extends Declaration {
	private Name N;
	private ProcedureResult D1;
	private ArgumentDeclaration D2;

	public ProcedureHeaderDeclaration(Name n, ArgumentDeclaration a, ProcedureResult p) {
		N = n;
		D1 = p;
		D2 = a;
	}

	

	/**
	 * @return the n
	 */
	public String getName() {
	    return N.value();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	       	return vis.visitProcedureHeaderDeclaration(this, attr);	   
	   }

	/**
	 * @return the d1
	 */
	public ProcedureResult getProcedureResult() {
	    return D1;
	}



	/**
	 * @return the d2
	 */
	public SingleArgumentDeclaration[] getProcedureArguments() {
	    return D2.flattenArguments();
	}
}
