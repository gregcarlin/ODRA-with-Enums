/**
 * 
 */
package odra.sbql.optimizers.queryrewrite.independentquery;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.system.config.ConfigDebug;

/**
 * IndependencyChecker 
 * check if the name is indepentend from the non-algebraic operator
 * (context).
 * @author radamus
 *last modified: 2006-11-30
 */
public class IndependencyChecker extends TraversingASTAdapter {
	boolean isIndependent;
	private NonAlgebraicExpression context; //non algebraic operator for which we check
											//whether the name is independent

	/**
	 * @param context - non-algebraic operator - the context for independency 
	 * checking process
	 */
	public IndependencyChecker(NonAlgebraicExpression context) {
		if(ConfigDebug.ASSERTS) assert context.getEnvsInfo() != null : "the ENVS binding levels are not present in the AST";
		this.context = context;
		isIndependent = true;
	}
	
	public Object visitNameExpression(NameExpression expr, Object attr)
			throws SBQLException {

		//if name was bound in the environment created by the 'context' non-algebraic operator
		//the name is not independent
		
		if ((expr.getBindingInfo().boundat >= context.getEnvsInfo().baseEnvsSize) && (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize + context.getEnvsInfo().framesOpened))
			isIndependent = false;

		return null;

	}

	public boolean isIndependent()
	{
		return isIndependent;
	}
}
