package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.PrimitiveConditionPattern;

/**
 * A class for searching for primitive conditions.
 * 
 * @author jacenty
 * @version 2007-02-23
 * @since 2007-02-23
 */
public class PrimitiveConditionFinder extends ASTNodeFinder
{
	public PrimitiveConditionFinder()
	{
		super(new PrimitiveConditionPattern(), true);
	}
}
