package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.NameExpressionPattern;

/**
 * A class for searching for name expressions.
 * 
 * @author jacenty
 * @version 2007-04-07
 * @since 2007-04-07
 */
public class NameExpressionFinder extends ASTNodeFinder
{
	/**
	 * The constructor.
	 */
	public NameExpressionFinder()
	{
		super(new NameExpressionPattern(), true);
	}
}
