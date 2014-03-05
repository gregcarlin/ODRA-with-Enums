package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class InterfaceFieldDeclaration extends SingleFieldDeclaration {
	private InterfaceDeclaration D;
	
	/**
	 * @param d
	 */
	public InterfaceFieldDeclaration(InterfaceDeclaration d) {
		D = d;
	}

	public String getName() {
		return this.D.getInterfaceName();
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return D;
	}

	public String getInstanceName() {
		return this.D.getInstanceName();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitInterfaceFieldDeclaration(this, attr);
	}

	/**
	 * @return the d
	 */
	public InterfaceDeclaration getInterfaceDeclaration() {
	    return D;
	}

	/**
	 * @param d the d to set
	 */
	public void setInterfaceDeclaration(InterfaceDeclaration d) {
	    D = d;
	}
}
