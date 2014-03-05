package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialArgumentDeclaration extends ArgumentDeclaration {
	private ArgumentDeclaration D1, D2;
	
	public SequentialArgumentDeclaration(ArgumentDeclaration d1, ArgumentDeclaration d2) {
		D1 = d1;
		D2 = d2;
	}
	
	public SingleArgumentDeclaration[] flattenArguments() {
		Vector v = new Vector();
		
		for (SingleArgumentDeclaration a : D1.flattenArguments())
			v.addElement(a);
		
		for (SingleArgumentDeclaration a : D2.flattenArguments())
			v.addElement(a);

		return (SingleArgumentDeclaration[]) v.toArray(new SingleArgumentDeclaration[v.size()]);
	}
}
