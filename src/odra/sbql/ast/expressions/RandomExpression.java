package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * Random integer value expression.
 * 
 * @author jacenty
 * @version 2007-03-23
 * @since 2007-03-23
 */
public class RandomExpression extends BinaryExpression
{
	public RandomExpression(Expression min, Expression max)
	{
		super(min, max);
	}

	@Override
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitRandomExpression(this, attr);
	}
}
