package odra.sbql.ast.declarations;

public class EmptyInterfaceFieldFlagDeclaration extends InterfaceFieldFlagDeclaration {
	public int encodeFlag() {
		return 1 | 2 | 4 | 8; // CRUD
	}

	public SingleInterfaceFieldFlagDeclaration[] flattenFlags() {
		return new SingleInterfaceFieldFlagDeclaration[0];
	}
}
