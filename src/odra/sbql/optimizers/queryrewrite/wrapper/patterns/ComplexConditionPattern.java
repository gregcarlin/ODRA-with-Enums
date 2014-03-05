package odra.sbql.optimizers.queryrewrite.wrapper.patterns;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * A local pattern implementation for complex condition finding.
 * 
 * @author jacenty
 * @version 2007-06-27
 * @since 2007-02-23
 */
public class ComplexConditionPattern implements Pattern
{
	private final ASTNode rootToSkip;
	
	public ComplexConditionPattern(ASTNode rootToSkip)
	{
		this.rootToSkip = rootToSkip;
	}
	
	public boolean matches(Object obj)
	{
		if(obj.equals(rootToSkip))
			return false;
		
		return 
			obj instanceof SimpleBinaryExpression || 
			obj instanceof EqualityExpression ||
			obj instanceof WhereExpression;
	}
}
