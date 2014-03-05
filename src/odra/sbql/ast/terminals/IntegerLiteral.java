package odra.sbql.ast.terminals;

/**
 * AST node for integer literals in the source code (e.g. 1, 5543).
 * 
 * @author raist
 */

public class IntegerLiteral extends Terminal {
	private int V;

	public IntegerLiteral(int val) {
		V = val;
	}

	/**
	 * @return the v
	 */
	public int value()
	{
	    return V;
	}
}
