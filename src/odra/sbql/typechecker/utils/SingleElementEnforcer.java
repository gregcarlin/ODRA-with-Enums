/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ToSingleExpression;

/**
 * SingleElementEnforcer
 * @author Radek Adamus
 *@since 2008-08-27
 *last modified: 2008-08-27
 *@version 1.0
 */
public class SingleElementEnforcer extends ASTExpressionEnforcer {

	/**
	 * @param astVisitor
	 */
	public SingleElementEnforcer(ASTVisitor astVisitor) {
		super(astVisitor);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#enforceInternal(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected Expression createEnforcedNode(Expression node) {
		// TODO Auto-generated method stub
		return new ToSingleExpression(node);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#enforceIsRequired(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected boolean enforceIsRequired(Expression node) {
		assert node.getSignature() != null : "node.getSignature() != null";
		if (node.getSignature().getMinCard() != 1 || node.getSignature().getMaxCard() != 1) 
			return true;
		return false;
	}

}
