package odra.sbql.optimizers.queryrewrite.unionquery;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;

/**
 * DependencyChecker search engine for union query optimization method
 * 
 * @author murlewski
 */
public class DependencyChecker extends TraversingASTAdapter
{
	boolean isDependent;

	/**
	 * the NonAlgebraic exgression which is tested whenever it is dependent
	 */
	private NonAlgebraicExpression context;

	public DependencyChecker(NonAlgebraicExpression context)
	{
		this.isDependent = true;
		this.context = context;
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{
		if (expr.getBindingInfo().boundat < context.getEnvsInfo().baseEnvsSize)
			isDependent = false;

		return null;

	}

}
