package odra.sbql.ast.expressions;

import java.util.Date;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.DateLiteral;

/**
 * Now expression (current date).
 * 
 * @author jacenty
 * @version 2007-03-21
 * @since 2007-03-21
 */
public class NowExpression extends DateExpression
{
	public NowExpression()
	{
		super(new DateLiteral(new Date()));
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException
	{
		return vis.visitDateExpression(this, attr);
	}
}
