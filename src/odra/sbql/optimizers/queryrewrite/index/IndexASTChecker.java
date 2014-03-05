package odra.sbql.optimizers.queryrewrite.index;

import odra.sbql.SBQLException;
import odra.sbql.ast.expressions.Expression;

/**
 * Class that performs following checks:<br>
 * <ul>
 * <li>if given key AST is associated non-key generator expression.  
 * Another words if key is generated direcly from nonkey value objects.</li>
 * <li>if given AST (e.g. taken from predicate condition) 
 * is not associated to non-key generator expression.</li>
 * <li>if given AST (e.g. taken from non-key) 
 * is associated direcly with database root.</li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
public class IndexASTChecker{

	private KeyGeneratorASTMarker marker = new KeyGeneratorASTMarker();
	private ASTBoundToChecker boundToChecker = new ASTBoundToChecker(marker.getGeneratorsMap());
	private ASTNotBoundToChecker notBoundToChecker = new ASTNotBoundToChecker(marker.getGeneratorsMap());

	public void markIndexSubAST(Expression expr) throws SBQLException {
		marker.markIndexSubAST(expr);
	}
	
	public void checkASTBoundTo(Expression expr, Expression boundToExpr) throws SBQLException {
		boundToChecker.checkASTBoundTo(expr, boundToExpr);
	}
	
	public boolean isWholeASTBoundedTo(Expression expr, Expression boundToExpr) {
		try {
			boundToChecker.checkASTBoundTo(expr, boundToExpr);
		} catch (Exception E) {
			return false;
		}
		return true;
	}
	
	public boolean isWholeASTNotBoundedTo(Expression expr, Expression boundToExpr) {
		try {
			notBoundToChecker.checkASTNotBoundTo(expr, boundToExpr);
		} catch (Exception E) {
			return false;
		}
		return true;
	}
		
}
