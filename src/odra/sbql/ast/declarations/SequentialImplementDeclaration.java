package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialImplementDeclaration extends ImplementDeclaration {
	public ImplementDeclaration D1, D2;
	
	public SequentialImplementDeclaration(ImplementDeclaration i1, ImplementDeclaration i2) {
		D1 = i1;
		D2 = i2;
	}

	public SingleImplementDeclaration[] flattenImplements() {
		Vector v = new Vector();
		
		for (SingleImplementDeclaration i : D1.flattenImplements())
			v.addElement(i);

		for (SingleImplementDeclaration i : D2.flattenImplements())
			v.addElement(i);

		return (SingleImplementDeclaration[]) v.toArray(new SingleImplementDeclaration[v.size()]);
	}
}
