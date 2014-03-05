package odra.sbql.ast.declarations;

public class EmptyImplementDeclaration extends ImplementDeclaration {
	public SingleImplementDeclaration[] flattenImplements() {
		return new SingleImplementDeclaration[0];
	}
}
