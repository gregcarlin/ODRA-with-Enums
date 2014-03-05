package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for "orderby" operator finding.
 * 
 * @author jacenty
 * @version 2008-01-29
 * @since 2008-01-29
 */
public class OrderByPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof OrderByExpression;
	}
}