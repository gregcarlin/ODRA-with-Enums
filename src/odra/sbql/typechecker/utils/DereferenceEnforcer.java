/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;

/**
 * DereferenceEnforcer
 * @author Radek Adamus
 *@since 2008-08-22
 *last modified: 2008-08-22
 *@version 1.0
 */
public class DereferenceEnforcer extends ASTExpressionEnforcer {

	/**
	 * @param astVisitor
	 * @param attr
	 */
	public DereferenceEnforcer(ASTVisitor astVisitor) {
		super(astVisitor);

	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.NodeEnforcer#enforceInternal(odra.sbql.ast.ASTNode)
	 */
	@Override
	protected Expression createEnforcedNode(Expression node) {		
		return new DerefExpression(node);
	}

	/* (non-Javadoc)
	 * @see odra.sbql.typechecker.utils.NodeEnforcer#requireEnforce(odra.sbql.ast.ASTNode)
	 */
	@Override
	protected boolean enforceIsRequired(Expression node) {
		Signature sig = node.getSignature();
		if(isReevaluation(node))
			return false;
		if(node.getSignature() instanceof ReferenceSignature && !((ReferenceSignature)sig).hasRefFlag()){
			return true;
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
