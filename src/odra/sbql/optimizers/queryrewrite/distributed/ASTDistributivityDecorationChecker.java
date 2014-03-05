package odra.sbql.optimizers.queryrewrite.distributed;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.optimizers.OptimizationException;
import odra.system.Names;

/**
 * Utility class analyzing subquery to determine if it is decorated with a given link.
 * 
 * 
 * @author janek
 * 
 */

public class ASTDistributivityDecorationChecker extends TraversingASTAdapter
{
	private boolean isSingleLinkDecorated = true;
	private OID linkOID;

	public ASTDistributivityDecorationChecker(OID l)
	{
		linkOID = l;
	}

	public boolean isSingleLinkDecorated()
	{
		return isSingleLinkDecorated;
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{
		if (!isDecoratedWithGivenLink(expr))
			isSingleLinkDecorated = false;

		return null;
	}

	@Override
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr) throws SBQLException
	{
		if (isDecoratedWithGivenLink(expr))
			return super.commonVisitUnaryExpression(expr, attr);
		else
		{
			isSingleLinkDecorated = false;
			return null;
		}
	}

	@Override
	protected Object commonVisitAlgebraicExpression(BinaryExpression expr, Object attr) throws SBQLException
	{
		if (isDecoratedWithGivenLink(expr))
			return super.commonVisitAlgebraicExpression(expr, attr);
		else
		{
			isSingleLinkDecorated = false;
			return null;
		}
	}

	@Override
	protected Object commonVisitNonAlgebraicExpression(NonAlgebraicExpression expr, Object attr) throws SBQLException
	{

		if (isDecoratedWithGivenLink(expr))
			return super.commonVisitNonAlgebraicExpression(expr, attr);
		else
		{
			isSingleLinkDecorated = false;
			return null;
		}
	}


	private boolean isDecoratedWithGivenLink(Expression expr) throws SBQLException
	{
		if (expr.links.size() == 1)
		{
			OID oid = expr.links.toArray(new OID[1])[0];
			try {
			    if ((oid.equals(linkOID)) && (!oid.getObjectName().equals(Names.namesstr[Names.LOCALHOST_LINK])))
			    {
			    	return true;
			    }
			} catch (DatabaseException e) {
			    throw new OptimizationException(e, expr,this);
			}
		}

		return false;
	}

}
