package odra.sbql.ast.declarations;


public abstract class ExtendsDeclaration extends Declaration {
    public abstract SingleExtendsDeclaration[] flattenExtends();

	
	
	
	protected ExtendsDeclaration() { super(); }
}
