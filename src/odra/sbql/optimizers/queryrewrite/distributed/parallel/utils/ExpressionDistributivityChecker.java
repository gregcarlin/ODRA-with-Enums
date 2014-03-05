package odra.sbql.optimizers.queryrewrite.distributed.parallel.utils;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.WhereExpression;

/**
 * @author janek
 * 
 */
public class ExpressionDistributivityChecker extends TraversingASTAdapter
{
	private boolean isDistributive;

	public ExpressionDistributivityChecker()
	{
		isDistributive = true;
	}
	
	@Override
	protected Object commonVisitBinaryExpression(BinaryExpression expr, Object attr) throws SBQLException
	{
		if (!isDistributive(expr))
		{
			isDistributive = false;
			return null;
		}

		return expr.getLeftExpression().accept(this, attr);
	}

	@Override
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr) throws SBQLException
	{
		return super.commonVisitUnaryExpression(expr, attr);
	}

	private boolean isDistributive(Expression expr)
	{
		if ((expr instanceof JoinExpression) || (expr instanceof DotExpression) || (expr instanceof WhereExpression))
		{
			return true;
		}

		return false;
	}

	public boolean isDistributive()
	{
		return isDistributive;
	}

}
