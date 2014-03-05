package odra.sbql.ast.declarations;

import java.util.Vector;

public class SequentialExtendsDeclaration extends ExtendsDeclaration {
	public ExtendsDeclaration D1, D2;

	/**
	 * @param N
	 * @param d
	 */
	public SequentialExtendsDeclaration(ExtendsDeclaration d1, ExtendsDeclaration d2) {		
		this.D1 = d1;
		this.D2 = d2;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.ExtendsDeclaration#flattenExtends()
	 */
	@Override
	public SingleExtendsDeclaration[] flattenExtends() {
	    
		Vector<SingleExtendsDeclaration> v = new Vector<SingleExtendsDeclaration>();
		
		for (SingleExtendsDeclaration i : D1.flattenExtends())
			v.addElement(i);

		for (SingleExtendsDeclaration i : D2.flattenExtends())
			v.addElement(i);

		return (SingleExtendsDeclaration[]) v.toArray(new SingleExtendsDeclaration[v.size()]);
	
	}
	
	
	
}
