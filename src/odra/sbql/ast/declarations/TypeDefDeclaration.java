package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Name;

public class TypeDefDeclaration extends Declaration {
	private TypeDeclaration D;
	private Name N;
	private boolean isDistinct = false;

	/**
	 * @param d
	 * @param n
	 * @param distinct
	 */
	public TypeDefDeclaration(Name n, TypeDeclaration d, boolean distinct) {
	    D = d;
	    N = n;
	    this.isDistinct = distinct;
	}

	public TypeDefDeclaration(Name n, TypeDeclaration d) {
		N = n;
		D = d;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitTypeDefDeclaration(this, attr);
	}
	
	public TypeDeclaration getTypeDeclaration(){
	    return D;
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
	public boolean isDistinct(){
	    return isDistinct;
	}

	/**
	 * @return the d
	 */
	public TypeDeclaration getType() {
	    return D;
	}
}
