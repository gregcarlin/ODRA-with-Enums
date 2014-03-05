package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

public class MethodFieldDeclaration extends ProcedureFieldDeclaration {
	public MethodFieldDeclaration(ProcedureDeclaration p) {
		super(p);
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitMethodFieldDeclaration(this, attr);
	}
}
