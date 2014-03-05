/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.results.compiletime.Signature;

/**
 * ProcedureCallEnforcer
 * @author Radek Adamus
 *@since 2008-08-28
 *last modified: 2008-08-28
 *@version 1.0
 */
public class ProcedureCallEnforcer extends ASTExpressionEnforcer {

	/**
	 * @param astVisitor
	 */
	public ProcedureCallEnforcer(ASTVisitor astVisitor) {
		super(astVisitor);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#createEnforcedNode(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected Expression createEnforcedNode(Expression node) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#enforceIsRequired(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected boolean enforceIsRequired(Expression node) {
		Signature sig = node.getSignature();
		Expression parent = node.getParentExpression();
		if(parent != null){
			if (parent instanceof RefExpression) {
				return false;
			}
			if (parent instanceof ProcedureCallExpression && ((ProcedureCallExpression) parent).getProcedureSelectorExpression().equals(node)) {
				return false; // this is already procedure selector expression so do
						// nothing
			}
		}
		return false;
	}
	private boolean isReevaluation(Expression node){
		if(node.isEnforced()){
			if(node instanceof DerefExpression)
				return true;
			return isReevaluation(((UnaryExpression)node).getExpression());
		}
		return false;
	}
}
