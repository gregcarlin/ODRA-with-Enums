package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class TypeDefFieldDeclaration extends SingleFieldDeclaration {
	private TypeDefDeclaration D;
	
	public TypeDefFieldDeclaration(TypeDefDeclaration d) {
		D = d;
	}

	public String getName() {
		return D.getName();
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return D;
	}
	public TypeDefDeclaration getTypeDefDeclaration(){
	    return D;
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitTypeDefFieldDeclaration(this, attr);
	}
}
