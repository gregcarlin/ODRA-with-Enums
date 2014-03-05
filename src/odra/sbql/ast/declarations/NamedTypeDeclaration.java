package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class NamedTypeDeclaration extends TypeDeclaration {
	private CompoundName N;
	
	public NamedTypeDeclaration(Name n) {
		this(new CompoundName(n));
	}
	public NamedTypeDeclaration(CompoundName n) {
		N =  n;
		this.setTypeName(n.nameAsString());
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitNamedTypeDeclaration(this, attr);
	}
	
	/**
	 * @return the n
	 */
	public CompoundName getName() {
	    return N;
	}
}
