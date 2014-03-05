package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialInterfaceFieldFlagDeclaration extends InterfaceFieldFlagDeclaration {
	private InterfaceFieldFlagDeclaration D1, D2;

	public SequentialInterfaceFieldFlagDeclaration(InterfaceFieldFlagDeclaration d1, InterfaceFieldFlagDeclaration d2) {
		D1 = d1;
		D2 = d2;
	}

	public int encodeFlag() {
		return D1.encodeFlag() | D2.encodeFlag();
	}

	public SingleInterfaceFieldFlagDeclaration[] flattenFlags() {
		Vector v = new Vector();

		for (InterfaceFieldFlagDeclaration f : D1.flattenFlags())
			v.addElement(f);

		for (InterfaceFieldFlagDeclaration f : D2.flattenFlags())
			v.addElement(f);

		return (SingleInterfaceFieldFlagDeclaration[]) v.toArray(new SingleInterfaceFieldFlagDeclaration[v.size()]);
	}	
}
