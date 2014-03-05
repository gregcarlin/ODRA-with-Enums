package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialImportDeclaration extends ImportDeclaration {
	public ImportDeclaration D1, D2;
	
	public SequentialImportDeclaration(ImportDeclaration i1, ImportDeclaration i2) {
		D1 = i1;
		D2 = i2;
	}

	public SingleImportDeclaration[] flattenImports() {
		Vector v = new Vector();
		
		for (SingleImportDeclaration i : D1.flattenImports())
			v.addElement(i);

		for (SingleImportDeclaration i : D2.flattenImports())
			v.addElement(i);

		return (SingleImportDeclaration[]) v.toArray(new SingleImportDeclaration[v.size()]);
	}
}
