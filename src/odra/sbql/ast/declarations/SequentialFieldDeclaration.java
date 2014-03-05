package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialFieldDeclaration extends FieldDeclaration {
	public FieldDeclaration D1, D2;
	
	public SequentialFieldDeclaration(FieldDeclaration f1, FieldDeclaration f2) {
		D1 = f1;
		D2 = f2;
	}
	
	public SingleFieldDeclaration[] flattenFields() {
		Vector v = new Vector();
		
		for (FieldDeclaration f : D1.flattenFields())
			v.addElement(f);
		
		for (FieldDeclaration f : D2.flattenFields())
			v.addElement(f);

		return (SingleFieldDeclaration[]) v.toArray(new SingleFieldDeclaration[v.size()]);
	}
}
