package odra.sbql.ast.terminals;

/**
 * AST node for boolean literals in the source code (e.g. true, false).
 * 
 * @author raist
 */

public class BooleanLiteral extends Terminal {
	private boolean V;

	public BooleanLiteral(boolean val) {
		V = val;
	}

	/**
	 * @return the v
	 */
	public boolean value()
	{
	    return V;
	}
}
