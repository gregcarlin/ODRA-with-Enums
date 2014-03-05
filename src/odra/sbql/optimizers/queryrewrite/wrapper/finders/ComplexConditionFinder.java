package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.ComplexConditionPattern;

/**
 * A class for searching for complec conditions.
 * 
 * @author jacenty
 * @version 2007-02-23
 * @since 2007-02-23
 */
public class ComplexConditionFinder extends ASTNodeFinder
{
	public ComplexConditionFinder(ASTNode rootToSkip)
	{
		super(new ComplexConditionPattern(rootToSkip), true);
	}
}
