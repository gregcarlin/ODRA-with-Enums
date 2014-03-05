package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for finding {@link WhereExpression} queries.
 * 
 * @author jacenty
 * @version 2007-02-21
 * @since 2007-02-21
 */
public class WherePattern implements Pattern
{
	/**
	 * The constructor 
	 */
	public WherePattern()
	{
		
	}
	
	public boolean matches(Object obj)
	{
		if(obj instanceof WhereExpression)
			return true;
		
		return false;
	}
}