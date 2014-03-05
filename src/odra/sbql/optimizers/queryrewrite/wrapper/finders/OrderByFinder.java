package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.OrderByPattern;

/**
 * A class for searching for a "orderby" operator.
 * 
 * @author jacenty
 * @version 2008-01-29
 * @since 2008-01-29
 */
public class OrderByFinder extends ASTNodeFinder
{
	public OrderByFinder()
	{
		super(new OrderByPattern(), true);
	}
}
