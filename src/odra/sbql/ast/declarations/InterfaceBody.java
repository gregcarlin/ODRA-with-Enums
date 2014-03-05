package odra.sbql.ast.declarations;


public class InterfaceBody extends Declaration {
	private FieldDeclaration D;
	
	public InterfaceBody(FieldDeclaration d) {
		D = d;
	}
	
	public SingleFieldDeclaration[] getFields(){
	    return D.flattenFields();
	}

	
}
