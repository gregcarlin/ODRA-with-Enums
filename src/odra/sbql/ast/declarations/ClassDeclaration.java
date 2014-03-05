package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class ClassDeclaration extends Declaration {
	private Name N;
	private ExtendsDeclaration E;
	private ClassBody B;
	/**
	 * @param n
	 * @param e
	 * @param b
	 */
	public ClassDeclaration(Name n, ExtendsDeclaration e, ClassBody b) {
		N = n;
		E = e;
		B = b;
	}
	
	/**
	 * @param n
	 * @param e
	 * @param b
	 */
	public ClassDeclaration(Name n, ClassBody b) {
		N = n;
		B = b;
		E = new EmptyExtendsDeclaration();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitClassDeclaration(this, attr);
	}
	
	public String[] getExtends(){
	    SingleExtendsDeclaration[] sexts = E.flattenExtends();
	    String[] extnames = new String[sexts.length];
	    for(int i = 0 ; i < extnames.length; i++){
		extnames[i] =  sexts[i].N.value();
	    }
	    return extnames;
	}
	
	public String getName(){
	    return N.value();
	}
	
	public void setName(String name){
	    N = new Name(name);
	}
	
	public ClassInstanceDeclaration getInstanceDeclaration(){
	    return B.getInstanceDeclaration();
	}
	
	public SingleFieldDeclaration[] getFieldsDeclaration(){
	    return B.getFieldsDeclaration();
	}
	
	public SingleImplementDeclaration[] getImplementsDeclaration(){
	    return B.getImplementDeclaration();
	}
}
