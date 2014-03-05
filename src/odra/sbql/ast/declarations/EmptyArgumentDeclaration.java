package odra.sbql.ast.declarations;

public class EmptyArgumentDeclaration extends ArgumentDeclaration {	
	public SingleArgumentDeclaration[] flattenArguments() {
		return new SingleArgumentDeclaration[0];
	}
}
