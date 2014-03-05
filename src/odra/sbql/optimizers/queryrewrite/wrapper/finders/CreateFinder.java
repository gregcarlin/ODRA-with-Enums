package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.CreatePattern;

/**
 * A class for searching for a "create" operator.
 * 
 * @author jacenty
 * @version 2008-01-26
 * @since 2008-01-26
 */
public class CreateFinder extends ASTNodeFinder
{
	public CreateFinder()
	{
		super(new CreatePattern(), true);
	}
}
