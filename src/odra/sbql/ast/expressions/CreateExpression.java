package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;



public class CreateExpression extends UnaryExpression {
	private Name N;
	private Expression E;
	private String blockName;
	
	//create permanent operation can concern imported module 
	public int importModuleRef = CURRENT_MODULE;
	//where the variable is declared 
	public int declaration_environment = UNKNOWN;
	
	/**
	 * @param e
	 */
	public CreateExpression(Name n,Expression e) {
		super(e);
		setName(n);		
	
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCreateExpression(this, attr);
	}
	
	/**
	 * @param n the n to set
	 */
	public void setName(Name n)
	{
	    N = n;
	}

	/**
	 * @return the n
	 */
	public Name name()
	{
	    return N;
	}

	/**
	 * @return the blockName
	 */
	public String getBlockName() {
	    return blockName;
	}

	/**
	 * @param blockName the blockName to set
	 */
	public void setBlockName(String blockName) {
	    this.blockName = blockName;
	}

	public static final int CURRENT_MODULE = -1;
	
	public static final int UNKNOWN = 0;
	public static final int LOCAL = 1;
	public static final int TEMPORAL = 2;
	public static final int PERSISTENT = 3;

	
}
