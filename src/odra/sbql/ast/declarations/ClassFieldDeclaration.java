package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class ClassFieldDeclaration extends SingleFieldDeclaration {
	private ClassDeclaration D;
	
	/**
	 * @param d
	 */
	public ClassFieldDeclaration(ClassDeclaration d) {
		D = d;
	}

	@Override
	public String getName() {
		return this.D.getName();
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return getClassDeclaration();
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitClassFieldDeclaration(this, attr);
	}

	/**
	 * @param d the d to set
	 */
	public void setClassDeclaration(ClassDeclaration d) {
	    D = d;
	}

	/**
	 * @return the d
	 */
	public ClassDeclaration getClassDeclaration() {
	    return D;
	}

}
