package odra.sbql.ast.declarations;


public abstract class ImportDeclaration extends Declaration {
	public abstract SingleImportDeclaration[] flattenImports();
}
