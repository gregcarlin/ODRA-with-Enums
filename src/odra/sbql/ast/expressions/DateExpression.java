package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.DateLiteral;

/**
 * Date expression.
 * 
 * @author jacenty
 * @version 2007-03-19
 * @since 2007-03-19
 */
public class DateExpression extends Expression
{
	private DateLiteral L;

	public DateExpression(DateLiteral l)
	{
		L = l;
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitDateExpression(this, attr);
	}

	/**
	 * @return the l
	 */
	public DateLiteral getLiteral()
	{
	    return L;
	}
}
