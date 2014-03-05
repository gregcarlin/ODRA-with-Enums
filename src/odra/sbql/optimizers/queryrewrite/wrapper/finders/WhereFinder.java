package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.WherePattern;

/**
 * A class for searching for {@link WhereExpression} queries.
 * 
 * @author jacenty
 * @version 2007-02-21
 * @since 2007-02-21
 */
public class WhereFinder extends ASTNodeFinder
{
	/**
	 * The constructor.
	 *  
	 */
	public WhereFinder()
	{
		super(new WherePattern(), true);
	}
}
