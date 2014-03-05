package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * Date precission expression.
 * 
 * @author jacenty
 * @version 2007-03-27
 * @since 2007-03-21
 */
public class DateprecissionExpression extends BinaryExpression
{
	/** YYYY-MM-DD */
	public static final String PRECISSION_LOW = "low";
	/** YYYY-MM-DD hh:mm */
	public static final String PRECISSION_MEDIUM = "medium";
	/** YYYY-MM-DD hh:mm:ss */
	public static final String PRECISSION_HIGH = "high";
	/** YYYY-MM-DD hh:mm:ss.000 - as a default one */
	public static final String PRECISSION_FULL = "full";
	
	public DateprecissionExpression(Expression e, StringExpression precission)
	{
		super(e, precission);
	}

	@Override
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitDateprecissionExpression(this, attr);
	}
}
