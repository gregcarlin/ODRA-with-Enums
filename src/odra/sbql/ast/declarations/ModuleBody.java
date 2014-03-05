package odra.sbql.ast.declarations;

public class ModuleBody {
	private FieldDeclaration D1;
	private ImportDeclaration D2;
	private ImplementDeclaration D3;
	
	public ModuleBody() {
		D1 = new EmptyFieldDeclaration();
		D2 = new EmptyImportDeclaration();
		D3 = new EmptyImplementDeclaration();
	}

	public ModuleBody(ImportDeclaration imp, ImplementDeclaration impl, FieldDeclaration fld) {
		D1 = fld;
		D2 = imp;
		D3 = impl;
	}
	
	public SingleFieldDeclaration[] getFieldDeclaration(){
	    return D1.flattenFields();
	}

	/**
	 * @return the d2
	 */
	public SingleImportDeclaration[] getImportDeclaration() {
	    return D2.flattenImports();
	}

	/**
	 * @return the d3
	 */
	public SingleImplementDeclaration[] getImplementDeclaration() {
	    return D3.flattenImplements();
	}
	
}
