package odra.sbql.ast.declarations;

import odra.db.schema.OdraClassSchema;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class ClassInstanceDeclaration extends Declaration{
	private Name N;
	private RecordTypeDeclaration D;
	/**
	 * @param n
	 * @param rd
	 */
	public ClassInstanceDeclaration(Name n, RecordDeclaration rd) {
		D = new RecordTypeDeclaration(rd);
		this.N = n;
	}
	
	/**
	 * @param n
	 * @param rtd
	 */
	public ClassInstanceDeclaration(Name n, RecordTypeDeclaration rtd) {
		D = rtd;
		this.N = n;
	}
	public ClassInstanceDeclaration(RecordDeclaration rd) {
		D = new RecordTypeDeclaration(rd);
		this.N = new Name(OdraClassSchema.NO_INVARIANT_NAME);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitClassInstanceDeclaration(this, attr);
	}

	/**
	 * @param n the n to set
	 */
	public void setInstanceName(String n) {
	    N =  new Name(n);
	}

	/**
	 * @return the n
	 */
	public String getInstanceName() {
	    return N.value();
	}

	/**
	 * @param d the d to set
	 */
	public void setInstanceType(RecordTypeDeclaration d) {
	    D = d;
	}

	/**
	 * @return the d
	 */
	public RecordTypeDeclaration getInstanceType() {
	    return D;
	}
}
