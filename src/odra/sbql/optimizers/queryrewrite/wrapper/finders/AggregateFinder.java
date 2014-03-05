package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.AggregatePattern;

/**
 * A class for searching for aggregate functions in a query.
 * 
 * @author jacenty
 * @version 2007-03-09
 * @since 2007-03-09
 */
public class AggregateFinder extends ASTNodeFinder
{
	public AggregateFinder()
	{
		super(new AggregatePattern(), false);
	}
}
