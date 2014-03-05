package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialEnumeratorDeclaration extends EnumeratorDeclaration{

	private EnumeratorDeclaration E1, E2;
		
		public SequentialEnumeratorDeclaration(EnumeratorDeclaration e1, EnumeratorDeclaration e2) {
			E1 = e1;
			E2 = e2;
		}
		
		public SingleEnumeratorDeclaration[] flattenEnumerators() {
			Vector v = new Vector();
			
			for (SingleEnumeratorDeclaration a : E1.flattenEnumerators())
				v.addElement(a);
			
			for (SingleEnumeratorDeclaration a : E2.flattenEnumerators())
				v.addElement(a);

			return (SingleEnumeratorDeclaration[]) v.toArray(new SingleEnumeratorDeclaration[v.size()]);
		}
		

	}
