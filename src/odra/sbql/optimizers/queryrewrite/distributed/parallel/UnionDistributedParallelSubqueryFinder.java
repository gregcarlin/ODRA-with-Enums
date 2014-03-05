package odra.sbql.optimizers.queryrewrite.distributed.parallel;

import java.util.ArrayList;
import java.util.List;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.optimizers.queryrewrite.distributed.parallel.utils.ExpressionDistributivityChecker;
import odra.system.Names;

/**
 * @author janek
 * 
 */
class UnionDistributedParallelSubqueryFinder extends TraversingASTAdapter
{
	private List<Expression> parallelExpressions;
	private boolean isParallel;
	private UnionExpression root;

	public UnionDistributedParallelSubqueryFinder(UnionExpression expr)
	{
		parallelExpressions = new ArrayList<Expression>();
		isParallel = true;
		root = expr;
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException
	{
		if (!isParallel)
			return null;

		// TODO : check if RemoteExpressoin

		if (checkIfApplies(expr.getRightExpression()))
		{
			parallelExpressions.add(expr.getRightExpression());

			if (expr.getLeftExpression() instanceof UnionExpression)
			{
				return visitUnionExpression((UnionExpression) expr.getLeftExpression(), attr);
			}
			else
			{
				if (checkIfApplies(expr.getLeftExpression()))
					parallelExpressions.add(expr.getLeftExpression());
				else
					isParallel = false;
			}
		}
		else
		{
			isParallel = false;
		}

		return null;
	}

	private boolean checkIfApplies(Expression expr) throws SBQLException
	{
		if (checkDistributivityProperty(expr) && chceckSingleLinkDecorated(expr))
			return true;
		else
			return false;
	}


	private boolean chceckSingleLinkDecorated(Expression expr) throws SBQLException
	{
		if (expr instanceof RemoteQueryExpression)
		{
			return true;
		}
		else
			return false;
	}

	private boolean isDecoratedWithSingleLink(Expression expr) throws DatabaseException
	{
		if (expr.links.size() == 1)
		{
			OID oid = expr.links.toArray(new OID[1])[0];
			if (!oid.getObjectName().equals(Names.namesstr[Names.LOCALHOST_LINK]))
			{
				return true;
			}
		}

		return false;
	}

	private OID getLink(Expression expr) throws DatabaseException
	{
		OID link = null;

		if (isDecoratedWithSingleLink(expr))
			link = expr.links.toArray(new OID[1])[0];

		return link;
	}

	private boolean checkDistributivityProperty(Expression expr) throws SBQLException
	{
		ExpressionDistributivityChecker checker = new ExpressionDistributivityChecker();
		expr.accept(checker, null);

		if (checker.isDistributive())
			return true;
		else
			return false;
	}

	public List<Expression> getParallelExpressions()
	{
		return parallelExpressions;
	}

	public boolean isParallel()
	{
		return isParallel;
	}

	
}
