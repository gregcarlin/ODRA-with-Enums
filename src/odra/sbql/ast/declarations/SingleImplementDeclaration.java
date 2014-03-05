package odra.sbql.ast.declarations;

import odra.sbql.ast.terminals.Name;

public class SingleImplementDeclaration extends ImplementDeclaration {
	private Name N;
	
	public SingleImplementDeclaration(Name n) {
		N = n;
	}

	public SingleImplementDeclaration[] flattenImplements() {
		return new SingleImplementDeclaration[] { this };
	}
	

	/**
	 * @return the n
	 */
	public String getName() {
	    return N.value();
	}
}
