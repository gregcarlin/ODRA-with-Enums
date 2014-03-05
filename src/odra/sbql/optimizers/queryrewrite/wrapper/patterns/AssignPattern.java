package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for "assign" operator finding.
 * 
 * @author jacenty
 * @version 2007-02-26
 * @since 2007-02-26
 */
public class AssignPattern implements Pattern
{
	public boolean matches(Object obj)
	{
		return obj instanceof AssignExpression;
	}
}