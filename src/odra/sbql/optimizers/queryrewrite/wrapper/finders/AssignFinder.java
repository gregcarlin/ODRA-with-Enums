package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.AssignPattern;

/**
 * A class for searching for an "assign" operator.
 * 
 * @author jacenty
 * @version 2007-02-26
 * @since 2007-02-26
 */
public class AssignFinder extends ASTNodeFinder
{
	public AssignFinder()
	{
		super(new AssignPattern(), true);
	}
}
