package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for name expression finding.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
public class NameExpressionPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof NameExpression;
	}
}
