package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for finding {@link RangeExpression} queries.
 * 
 * @author jacenty
 * @version 2007-05-24
 * @since 2007-05-24
 */
public class RangeExpressionPattern implements Pattern
{
	/**
	 * The constructor 
	 */
	public RangeExpressionPattern()
	{
		
	}
	
	public boolean matches(Object obj)
	{
		if(obj instanceof RangeExpression)
			return true;
		
		return false;
	}
}