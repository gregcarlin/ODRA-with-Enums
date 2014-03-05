package odra.wrapper.sql.builder;

import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.UnaryExpression;

/**
 * Aggragate functions. 
 * 
 * @author jacenty
 * @version 2007-03-09
 * @since 2007-03-09
 */
public enum Aggregate
{
	COUNT("count"),
	SUM("sum"),
	MIN("min"),
	MAX("max"),
	AVG("avg");
	
	private final String function;
	
	Aggregate(String function)
	{
		this.function = function;
	}
	
	public String getFunction()
	{
		return function;
	}
	
	public static Aggregate getAggregateForString(String function)
	{
		if(function.equals(COUNT.getFunction()))
			return COUNT;
		else if(function.equals(SUM.getFunction()))
			return SUM;
		else if(function.equals(MIN.getFunction()))
			return MIN;
		else if(function.equals(MAX.getFunction()))
			return MAX;
		else if(function.equals(AVG.getFunction()))
			return AVG;
		else 
			throw new AssertionError("Unknown aggregate function: " + function);
	}
	
	public static Aggregate getAggregateForExpression(UnaryExpression expression)
	{
		if(expression instanceof CountExpression)
			return COUNT;
		else if(expression instanceof SumExpression)
			return SUM;
		else if(expression instanceof MinExpression)
			return MIN;
		else if(expression instanceof MaxExpression)
			return MAX;
		else if(expression instanceof AvgExpression)
			return AVG;
		else 
			throw new AssertionError("Unsupported aggregate expression: " + expression);
	}
}
