package odra.sbql.ast.declarations;


public class SingleImportDeclaration extends ImportDeclaration {
	public CompoundName N;
	
	public SingleImportDeclaration(CompoundName n) {
		N = n;
	}

	public SingleImportDeclaration[] flattenImports() {
		return new SingleImportDeclaration[] { this };
	}
}
