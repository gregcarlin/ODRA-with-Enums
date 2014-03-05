package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class EnumDeclaration extends Declaration {
	private Name N;
	private CompoundName T;
	private EnumeratorDeclaration EN;
	
	/**
	 * @param n
	 * @param t
	 * @param en
	 */
	public EnumDeclaration(Name n, CompoundName t, EnumeratorDeclaration en) {
		N = n;
		T = t;
		EN = en;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitEnumDeclaration(this, attr);
	}
	
	public String getName(){
	    return N.value();
	}
	
	public String getBaseTypeName(){
		return T.nameAsString();
	}
	
	public EnumeratorDeclaration getListEnumeratorDeclaration(){
		return EN;
	}
	
}
