package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for primitive condition finding.
 * 
 * @author jacenty
 * @version 2007-06-27
 * @since 2007-02-23
 */
public class PrimitiveConditionPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		if(obj instanceof EqualityExpression)
			return true;
		
		
		if(obj instanceof SimpleBinaryExpression)
		{
			Operator operator = ((SimpleBinaryExpression)obj).O;
			if(
					operator.getAsInt() == Operator.EQUALS ||
					operator.getAsInt() == Operator.DIFFERENT ||
					operator.getAsInt() == Operator.LOWER ||
					operator.getAsInt() == Operator.LOWEREQUALS ||
					operator.getAsInt() == Operator.GREATER ||
					operator.getAsInt() == Operator.GREATEREQUALS ||
					operator.getAsInt() == Operator.MATCH_STRING ||
					operator.getAsInt() == Operator.NOT_MATCH_STRING)
				
				return true;
		}
		
		return false;
	}
}
