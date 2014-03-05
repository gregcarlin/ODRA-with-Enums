package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.terminals.Name;
import odra.transactions.ast.IASTTransactionCapabilities;

/**
 * Represents the proper declaration of a procedure in the abstract syntax tree.
 * 
 * @author raist, radamus
 */
public class ProcedureDeclaration extends Declaration {
	
    	private ProcedureHeaderDeclaration header;

	private Statement S;

	
	/**
	 * @param header
	 * @param s
	 */
	public ProcedureDeclaration(ProcedureHeaderDeclaration header, Statement s) {
	    this.header = header;
	    S = s;
	}

	public ProcedureDeclaration(Name n, ArgumentDeclaration a, ProcedureResult p, Statement s) {
	    this.header = new ProcedureHeaderDeclaration(n,a,p);
		
		S = s;
	}

	public ProcedureDeclaration(Name n, ArgumentDeclaration a, ProcedureResult p, Statement s,
				IASTTransactionCapabilities capsASTTransaction) {
		super(capsASTTransaction);
		this.header = new ProcedureHeaderDeclaration(n,a,p);
		S = s;
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitProcedureDeclaration(this, attr);
	}

	public final Statement getStatement() {
		return this.S;
	}

	

	/**
	 * @return the procedure name
	 */
	public String getName() {
	    return header.getName();
	}

	/**
	 * @param s the s to set
	 */
	public void setStatement(Statement s) {
	    S = s;
	}
	
	public final SingleArgumentDeclaration[] getProcedureArguments(){
	    return header.getProcedureArguments();
	}
	
	public final ProcedureResult getProcedureResult(){
	 return this.header.getProcedureResult();   
	}

	public final ProcedureHeaderDeclaration getProcedureHeader(){
	 return header;   
	}
}