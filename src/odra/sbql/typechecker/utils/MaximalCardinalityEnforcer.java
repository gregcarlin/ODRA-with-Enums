/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.Expression;

/**
 * MaximalCardinalityEnforcer
 * @author Radek Adamus
 *@since 2008-08-27
 *last modified: 2008-08-27
 *@version 1.0
 */
public class MaximalCardinalityEnforcer extends ASTExpressionEnforcer {

	
	private final int maxCard;

	/**
	 * @param astVisitor
	 */
	public MaximalCardinalityEnforcer(ASTVisitor astVisitor, int maxCard) {
		super(astVisitor);
		// TODO Auto-generated constructor stub
		this.maxCard = maxCard;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#createEnforcedNode(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected Expression createEnforcedNode(Expression node) {
		return new AtMostExpression(node, maxCard);		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTExpressionEnforcer#enforceIsRequired(odra.sbql.ast.expressions.Expression)
	 */
	@Override
	protected boolean enforceIsRequired(Expression node) {
		if (node.getSignature().getMaxCard() > maxCard)
			return true;
		return false;
	}

}
