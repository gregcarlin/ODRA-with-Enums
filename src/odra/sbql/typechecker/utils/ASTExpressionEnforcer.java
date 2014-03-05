/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;

/**
 * ASTExpressionEnforcer
 * the subclass of ASTNode enforcer
 * specific for Expression nodes
 * @author Radek Adamus
 *@since 2008-08-27
 *last modified: 2008-08-27
 *@version 1.0
 */
public abstract class ASTExpressionEnforcer extends ASTNodeEnforcer<Expression> {

	/**
	 * @param astVisitor
	 */
	public ASTExpressionEnforcer(ASTVisitor astVisitor) {
		super(astVisitor);
	}

	
	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.ASTNodeEnforcer#enforceInternal(odra.sbql.ast.ASTNode)
	 */
	@Override
	protected final Expression enforceInternal(Expression node) {
		Expression parent = node.getParentExpression();
		Expression enforcedNode = createEnforcedNode(node);
		enforcedNode.setEnforced(true);
		if(parent != null)
			parent.replaceSubexpr(node, enforcedNode);
		return enforcedNode;
	}
	
	/**
	 * @param node - the expression that the enforce is based on
	 * @return enforced expression node
	 */
	protected abstract Expression createEnforcedNode(Expression node);
	
	

}
