package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for aggregate function finding.
 * 
 * @author jacenty
 * @version 2007-03-09
 * @since 2007-03-09
 */
public class AggregatePattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return
			obj instanceof CountExpression || 
			obj instanceof MinExpression || 
			obj instanceof MaxExpression ||
			obj instanceof SumExpression ||
			obj instanceof AvgExpression;
	}
}
