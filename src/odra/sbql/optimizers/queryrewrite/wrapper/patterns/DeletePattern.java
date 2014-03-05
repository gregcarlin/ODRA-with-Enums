package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for {@link DeleteExpression} finding.
 * 
 * @author jacenty
 * @version 2007-07-14
 * @since 2007-07-14
 */
public class DeletePattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof DeleteExpression;
	}
}