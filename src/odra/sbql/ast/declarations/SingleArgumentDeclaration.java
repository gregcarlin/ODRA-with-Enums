package odra.sbql.ast.declarations;

public class SingleArgumentDeclaration extends ArgumentDeclaration {
	public VariableDeclaration D;
	
	public SingleArgumentDeclaration(VariableDeclaration d) {
		D = d;
	}
	
	public SingleArgumentDeclaration[] flattenArguments() {
		return new SingleArgumentDeclaration[] { this };
	}
	
	
}
