package odra.sbql.ast.declarations;


public class VariableInterfaceFieldDeclaration extends VariableFieldDeclaration {
	public InterfaceFieldFlagDeclaration D2;
	
	public VariableInterfaceFieldDeclaration(VariableDeclaration var, InterfaceFieldFlagDeclaration flg) {
		super(var);
		
		D2 = flg;
	}
	
//	public Object accept(ASTVisitor vis, Object attr) throws Exception {
//		return vis.visitVariableFieldDeclaration(this, attr);
//	}
}
