package odra.sbql.ast.declarations;

public class EmptyFieldDeclaration extends FieldDeclaration {
	public SingleFieldDeclaration[] flattenFields() {
		return new SingleFieldDeclaration[0];
	}
}
