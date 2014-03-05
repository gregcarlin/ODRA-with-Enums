/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.Expression;

/**
 * MinimalCardinalityEnforcer
 * @author Radek Adamus
 *@since 2008-08-28
 *last modified: 2008-08-28
 *@version 1.0
 */
public class MinimalCardinalityEnforcer extends ASTExpressionEnforcer {

	private final int minCard;

	/**
	 * @param astVisitor
	 */
	public MinimalCardinalityEnforcer(ASTVisitor astVisitor, int minCard) {
		super(astVisitor);
		this.minCard = minCard;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#createEnforcedNode(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected Expression createEnforcedNode(Expression node) {		
		return new AtLeastExpression(node, minCard);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#enforceIsRequired(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected boolean enforceIsRequired(Expression node) {
		if (node.getSignature().getMinCard() < minCard)
			return true;
		return false;
	}

}
