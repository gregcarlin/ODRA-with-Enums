package odra.sbql.ast.terminals;

/**
 * AST node for string literals in the source code (e.g. "john", "cat").
 * 
 * @author raist
 */

public class StringLiteral extends Terminal {
	private String V;

	public StringLiteral(String val) {
		V = val;
	}

	/**
	 * @return the v
	 */
	public String value()
	{
	    return V;
	}
}
