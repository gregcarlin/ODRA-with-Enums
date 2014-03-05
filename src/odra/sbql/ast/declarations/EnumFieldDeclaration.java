package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class EnumFieldDeclaration extends SingleFieldDeclaration {
	private EnumDeclaration E;
	
	/**
	 * @param e
	 */
	public EnumFieldDeclaration(EnumDeclaration e) {
		E = e;
	}

	@Override
	public String getName() {
		return this.E.getName();
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return getEnumDeclaration();
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitEnumFieldDeclaration(this, attr);
	}

	/**
	 * @param e the d to set
	 */
	public void setEnumDeclaration(EnumDeclaration e) {
	    E = e;
	}

	/**
	 * @return the d
	 */
	public EnumDeclaration getEnumDeclaration() {
	    return E;
	}

}
