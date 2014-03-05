package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.JoinPattern;

/**
 * A class for searching for {@link JoinExpression} queries.
 * 
 * @author jacenty
 * @version 2007-03-01
 * @since 2007-03-01
 */
public class JoinFinder extends ASTNodeFinder
{
	/**
	 * The constructor.
	 *  
	 */
	public JoinFinder()
	{
		super(new JoinPattern(), true);
	}
}
