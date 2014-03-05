package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class InterfaceDeclaration extends Declaration {
	private Name N2;
	private Name N1;
	private ExtendsDeclaration E;
	private InterfaceBody interfaceBody;

	public InterfaceDeclaration(Name n1, Name n2, ExtendsDeclaration e, InterfaceBody b) {
		N1 = n1;
		N2 = n2;
		E = e;
		interfaceBody = b;
	}

	public InterfaceDeclaration(Name n1, Name n2, InterfaceBody b) {
		N1 = n1;
		N2 = n2;
		interfaceBody = b;
		E = new EmptyExtendsDeclaration();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitInterfaceDeclaration(this, attr);
	}
	
	public String[] getExtends(){
	    
	    SingleExtendsDeclaration[] sexts = E.flattenExtends();
	    String[] extnames = new String[sexts.length];
	    for(SingleExtendsDeclaration d : sexts){
		d.N.value();
	    }
	    return extnames;
	}

	

	/**
	 * @return the b
	 */
	public InterfaceBody getInterfaceBody() {
	    return interfaceBody;
	}

	

	/**
	 * @return the n1
	 */
	public String getInterfaceName() {
	    return N1.value();
	}



	/**
	 * @return the n2
	 */
	public String getInstanceName() {
	    return N2.value();
	}
}
