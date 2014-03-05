package odra.sbql.optimizers.queryrewrite.wrapper.finders;

import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.utils.ASTNodeFinder;
import odra.sbql.optimizers.queryrewrite.wrapper.patterns.ExecSqlPattern;

/**
 * A class for searching for an {@link ExecSqlExpression}.
 * 
 * @author jacenty
 * @version 2007-06-30
 * @since 2007-06-30
 */
public class ExecSqlFinder extends ASTNodeFinder
{
	public ExecSqlFinder()
	{
		super(new ExecSqlPattern(), false);
	}
}
