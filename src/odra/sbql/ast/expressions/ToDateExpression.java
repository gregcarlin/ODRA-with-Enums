package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * To-date coercion expression.
 * 
 * @author jacenty
 * @version 2007-03-19
 * @since 2007-03-19
 */
public class ToDateExpression extends UnaryExpression
{
	public ToDateExpression(Expression e)
	{
		super(e);
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitToDateExpression(this, attr);
	}
}
