package odra.sbql.optimizers.queryrewrite.distributed.parallel;

import java.util.List;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.ParallelExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.typechecker.SBQLTypeChecker;

/**
 * @author janek
 * 
 */
public class DistributedParallelQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer
{
	int nameSuffix;
	ASTNode root;

	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException
	{
		nameSuffix = 0;
		root = query;		
		root.accept(this, null);
		
		SBQLTypeChecker checker = new SBQLTypeChecker(module);
		query = checker.typecheckAdHocQuery(root);
				
		return root;
	}

	public void reset()
	{
	}

	public void setStaticEval(ASTAdapter staticEval)
	{
	}

	private NonAlgebraicExpression directNonAlgebraicOperator(Expression expr)
	{
		Expression e = expr.getParentExpression();
		while (!(e instanceof NonAlgebraicExpression))
		{
			if (e == null)
				return null;
			e = e.getParentExpression();
		}

		if (e instanceof NonAlgebraicExpression)
			return (NonAlgebraicExpression) e;
		else
			return null;
	}
	
	
	

	@Override
	public Object visitCountExpression(CountExpression expr, Object attr) throws SBQLException
	{
		super.visitCountExpression(expr, attr);		

		ParallelExpression pe = checkIfDirectIsParallel(expr);
		if (pe != null)
		{		
			SumExpression sum = new SumExpression(expr.getExpression());
			sum.setSignature(expr.getSignature());
			pe.setSignature(expr.getSignature());
			
			if (expr.getParentExpression() != null)
			{
				expr.getParentExpression().replaceSubexpr(expr, sum);
			}		
			else
			{
				this.root = sum;
			}			
			
			for (Expression e : pe.getParallelExpressions())
			{
				if ( e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression)e;
					UnaryExpression se = new CountExpression(remoteQuery.getExpression());
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), se);
				}
				else
					throw new RuntimeException("inject is not permited");
			}				
		}
		
		return null;
	}

	
	
	@Override
	public Object visitMaxExpression(MaxExpression expr, Object attr) throws SBQLException
	{
		super.visitMaxExpression(expr, attr);

		ParallelExpression pe = checkIfDirectIsParallel(expr);
		if (pe != null)
		{
			for (Expression e : pe.getParallelExpressions())
			{
				if (e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression) e;
					UnaryExpression se = new MaxExpression(remoteQuery.getExpression());
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), se);
				}
				else
					throw new RuntimeException("inject is not permited");
			}
			removeDeref(expr);
		}

		return null;
	}

	@Override
	public Object visitMinExpression(MinExpression expr, Object attr) throws SBQLException
	{
		super.visitMinExpression(expr, attr);

		ParallelExpression pe = checkIfDirectIsParallel(expr);
		if (pe != null)
		{
			for (Expression e : pe.getParallelExpressions())
			{
				if (e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression) e;
					UnaryExpression se = new MinExpression(remoteQuery.getExpression());
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), se);
				}
				else
					throw new RuntimeException("inject is not permited");
			}
			removeDeref(expr);
		}

		return null;
	}

	@Override
	public Object visitSumExpression(SumExpression expr, Object attr) throws SBQLException
	{
		super.visitSumExpression(expr, attr);

		ParallelExpression pe = checkIfDirectIsParallel(expr);
		if (pe != null)
		{
			for (Expression e : pe.getParallelExpressions())
			{
				if (e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression) e;
					UnaryExpression se = new SumExpression(remoteQuery.getExpression());
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), se);
				}
				else
					throw new RuntimeException("inject is not permited");
			}
			removeDeref(expr);
		}

		return null;
	}

	@Override
	public Object visitUnionExpression(UnionExpression expr, Object attr) throws SBQLException
	{

		UnionDistributedParallelSubqueryFinder unionOptimizer = new UnionDistributedParallelSubqueryFinder(expr);
		expr.accept(unionOptimizer, null);

		if (unionOptimizer.isParallel())
		{
			Expression newExpr = pushParallelUnionExpression(expr, unionOptimizer.getParallelExpressions());

			if (newExpr instanceof ParallelUnionExpression)
				this.root = newExpr;

			return null;
		}
		else
			return super.visitUnionExpression(expr, attr);
	}
	
	
	
	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException
	{
		super.visitForAllExpression(expr, attr);

		ParallelExpression pe = checkIfDirectIsParallel(expr.getLeftExpression());
		if (pe != null)
		{
			for (Expression e : pe.getParallelExpressions())
			{
				if (e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression) e;
					ForAllExpression forAll = new ForAllExpression(remoteQuery.getExpression(), (Expression)DeepCopyAST.copy(expr.getRightExpression()) );
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), forAll);
				}
				else
					throw new RuntimeException("inject is not permited");
			}

			String auxname = "$aux_as_" + nameSuffix++;
			AsExpression asExpr = new AsExpression(pe, new Name(auxname));
			expr.setLeftExpression(asExpr);
			expr.setRightExpression(new EqualityExpression(new NameExpression(new Name(auxname)), new BooleanExpression(
					new BooleanLiteral(true)), Operator.opEquals));

		}

		return null;

	}

	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException
	{
		super.visitForSomeExpression(expr, attr);

		ParallelExpression pe = checkIfDirectIsParallel(expr.getLeftExpression());
		if (pe != null)
		{
			for (Expression e : pe.getParallelExpressions())
			{
				if (e instanceof RemoteQueryExpression)
				{
					RemoteQueryExpression remoteQuery = (RemoteQueryExpression) e;
					ForSomeExpression forSome = new ForSomeExpression(remoteQuery.getExpression(), (Expression)DeepCopyAST.copy(expr.getRightExpression()) );
					remoteQuery.replaceSubexpr(remoteQuery.getExpression(), forSome);
				}
				else
					throw new RuntimeException("inject is not permited");
			}

			String auxname = "$aux_as_" + nameSuffix++;
			AsExpression asExpr = new AsExpression(pe, new Name(auxname));
			expr.setLeftExpression(asExpr);
			expr.setRightExpression(new EqualityExpression(new NameExpression(new Name(auxname)), new BooleanExpression(
					new BooleanLiteral(true)), Operator.opEquals));

		}

		return null;
	}

	private void removeDeref(UnaryExpression expr)
	{
		if (expr.getExpression() instanceof DerefExpression)
		{
			DerefExpression de = (DerefExpression) expr.getExpression();
			if (de.getExpression() instanceof ParallelExpression)
			{
				ParallelExpression pe = (ParallelExpression) de.getExpression();

				pe.setSignature(de.getSignature());
				expr.replaceSubexpr(expr.getExpression(), pe);
			}
		}
	}

	private ParallelExpression checkIfDirectIsParallel(Expression e)
	{
		if ( e instanceof ParallelExpression)
		{
			return (ParallelExpression) e;
		}
		else
			return null;
	}
	
	private ParallelExpression checkIfDirectIsParallel(UnaryExpression e)
	{
		if (e.getExpression() instanceof ParallelExpression)
			return (ParallelExpression) e.getExpression();
		else if (e.getExpression() instanceof DerefExpression)
		{
			DerefExpression de = (DerefExpression) e.getExpression();
			
			if (de.getExpression() instanceof ParallelExpression)
				return (ParallelExpression) de.getExpression();
		}

		return null;
	}

	public Expression pushParallelUnionExpression(Expression root, List<Expression> parallelExpressions)
	{
		ParallelUnionExpression pu = new ParallelUnionExpression();
		for (Expression expr : parallelExpressions)
		{
			if (expr instanceof RemoteQueryExpression)
				((RemoteQueryExpression) expr).runAsynchronously();

			pu.addExpression(expr);
		}

		if (root.getParentExpression() == null)
		{
			return pu;
		}
		else
		{
			root.getParentExpression().replaceSubexpr(root, pu);
			return root;
		}
	}

	
}
