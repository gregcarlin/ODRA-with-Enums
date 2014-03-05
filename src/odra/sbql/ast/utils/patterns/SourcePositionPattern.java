/**
 * 
 */
package odra.sbql.ast.utils.patterns;

import odra.sbql.ast.ASTNode;

/**
 * SourcePositionPattern
 * @author Radek Adamus
 *@since 2008-08-12
 *last modified: 2008-08-12
 *@version 1.0
 */
public class SourcePositionPattern implements Pattern {
	private final int line;
	private final int column;
	/**
	 * 
	 */
	public SourcePositionPattern(int line, int column) {
		this.line = line;
		this.column = column;		
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.utils.patterns.Pattern#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object obj) {
		if (obj instanceof ASTNode) {
			ASTNode node = (ASTNode) obj;
			return ((node.line == this.line)&& (node.column == this.column));
			
		}
		return false;
	}

}
