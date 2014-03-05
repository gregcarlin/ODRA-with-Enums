package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class VariableFieldDeclaration extends SingleFieldDeclaration {
	private VariableDeclaration variableDeclaration;
	
	public VariableFieldDeclaration(VariableDeclaration var) {
		variableDeclaration = var;
	}
	
	public String getName() {
		return variableDeclaration.getName();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return getVariableDeclaration();
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitVariableFieldDeclaration(this, attr);
	}

	/**
	 * @param d the d to set
	 */
	public void setVariableDeclaration(VariableDeclaration d) {
	    variableDeclaration = d;
	}

	/**
	 * @return the d
	 */
	public VariableDeclaration getVariableDeclaration() {
	    return variableDeclaration;
	}
}
