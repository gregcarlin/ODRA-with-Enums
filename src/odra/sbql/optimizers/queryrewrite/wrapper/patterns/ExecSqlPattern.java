package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for {@link ExecSqlExpression} finding.
 * 
 * @author jacenty
 * @version 2007-06-30
 * @since 2007-06-30
 */
public class ExecSqlPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof ExecSqlExpression;
	}
}