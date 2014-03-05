package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class RecordDeclaration extends Declaration {
	private Name N;
	private FieldDeclaration D;
	
	public RecordDeclaration(FieldDeclaration f) {
		D = f;
	}
	
	public RecordDeclaration(Name n, FieldDeclaration f) {
		N = n;
		D = f;
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRecordDeclaration(this, attr);
	}
	
	public SingleFieldDeclaration[] getFieldsDeclaration(){
	    return D.flattenFields();
	    
	}

	/**
	 * @param n the n to set
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
