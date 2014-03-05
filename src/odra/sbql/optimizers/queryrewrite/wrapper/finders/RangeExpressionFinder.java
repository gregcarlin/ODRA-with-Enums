package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.RangeExpressionPattern;

/**
 * A class for searching for {@link RangeExpression} queries.
 * 
 * @author jacenty
 * @version 2007-05-24
 * @since 2007-05-24
 */
public class RangeExpressionFinder extends ASTNodeFinder
{
	/**
	 * The constructor.
	 *  
	 */
	public RangeExpressionFinder()
	{
		super(new RangeExpressionPattern(), true);
	}
}
