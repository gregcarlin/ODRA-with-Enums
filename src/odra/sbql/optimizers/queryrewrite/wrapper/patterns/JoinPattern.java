package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for finding {@link JoinExpression} queries.
 * 
 * @author jacenty
 * @version 2007-03-01
 * @since 2007-03-01
 */
public class JoinPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		if(obj instanceof JoinExpression)
			return true;
		
		return false;
	}
}