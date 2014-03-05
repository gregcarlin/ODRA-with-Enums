package odra.sbql.ast.declarations;

import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;

public class ProcedureResult {
	private TypeDeclaration D1;
	private CardinalityDeclaration D2;

	private int reflevel; 
	
	public ProcedureResult() {
		D1 = new NamedTypeDeclaration(new Name("void"));
		D2 = new CardinalityDeclaration(new IntegerLiteral(1), new IntegerLiteral(1));
		reflevel = 0;

	}
	
	public ProcedureResult(TypeDeclaration type, CardinalityDeclaration card) {
		D1 = type;
		D2 = card;
		reflevel = 0;
	}
	public ProcedureResult(TypeDeclaration type, CardinalityDeclaration card, int ref) {
		D1 = type;
		D2 = card;
		reflevel = ref;

	}
	
	public int getResultMinCard(){
	    return D2.getMinCard();
	}
	
	public int getResultMaxCard(){
	    return D2.getMaxCard();
	}
	
	public final TypeDeclaration getResultType(){
	    return D1;
	}

	/**
	 * @param d1 the d1 to set
	 */
	public final void setResultType(TypeDeclaration d1) {
	    D1 = d1;
	}

	/**
	 * @param reflevel the reflevel to set
	 */
	public void setReflevel(int reflevel) {
	    this.reflevel = reflevel;
	}

	/**
	 * @return the reflevel
	 */
	public int getReflevel() {
	    return reflevel;
	}

}
