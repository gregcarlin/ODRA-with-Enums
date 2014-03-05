package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.DeletePattern;

/**
 * A class for searching for a {@link DeleteExpression}.
 * 
 * @author jacenty
 * @version 2007-07-14
 * @since 2007-07-14
 */
public class DeleteFinder extends ASTNodeFinder
{
	public DeleteFinder()
	{
		super(new DeletePattern(), true);
	}
}
