package odra.sbql.ast.terminals;

/**
 * AST node for real literals in the source code (e.g. 1.5, 5543.244).
 * 
 * @author raist
 */

public class RealLiteral extends Terminal {
	private double V;

	public RealLiteral(double val) {
		V = val;
	}	

	/**
	 * @return the v
	 */
	public double value()
	{
	    return V;
	}
}
