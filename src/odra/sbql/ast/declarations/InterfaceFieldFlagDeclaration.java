package odra.sbql.ast.declarations;

public abstract class InterfaceFieldFlagDeclaration {
	public abstract SingleInterfaceFieldFlagDeclaration[] flattenFlags();
	public abstract int encodeFlag();
}
