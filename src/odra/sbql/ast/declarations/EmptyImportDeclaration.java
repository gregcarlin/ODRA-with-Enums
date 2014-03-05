package odra.sbql.ast.declarations;

public class EmptyImportDeclaration extends ImportDeclaration {
	public SingleImportDeclaration[] flattenImports() {
		return new SingleImportDeclaration[0];
	}
}
