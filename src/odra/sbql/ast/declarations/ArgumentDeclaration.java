package odra.sbql.ast.declarations;

public abstract class ArgumentDeclaration extends Declaration {
	public abstract SingleArgumentDeclaration[] flattenArguments();	
}
