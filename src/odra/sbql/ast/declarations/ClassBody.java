package odra.sbql.ast.declarations;

public class ClassBody extends Declaration {
	private ClassInstanceDeclaration D1;
	private FieldDeclaration D2;
	private ImplementDeclaration D3;

	public ClassBody(ClassInstanceDeclaration d1, ImplementDeclaration d3, FieldDeclaration d2) {
		D1 = d1;
		D2 = d2;
		D3 = d3;
	}	
	
	public final ClassInstanceDeclaration getInstanceDeclaration(){
	    return D1;
	}
	
	public final SingleFieldDeclaration[] getFieldsDeclaration(){
	    return D2.flattenFields();
	}
	
	public final SingleImplementDeclaration[] getImplementDeclaration(){
	    return D3.flattenImplements();
	}
}
