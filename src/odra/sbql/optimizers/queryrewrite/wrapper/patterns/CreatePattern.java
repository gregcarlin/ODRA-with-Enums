package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for "create" (and "create permanent") operator finding.
 * 
 * @author jacenty
 * @version 2008-01-26
 * @since 2008-01-26
 */
public class CreatePattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof CreateExpression;
	}
}