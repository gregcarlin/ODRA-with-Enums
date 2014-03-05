package odra.sbql.optimizers.queryrewrite.distributed;

import java.util.ArrayList;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.system.Names;

/**
 * An optimizer rewriting query into subqueries which are to be executed on
 * remote nodes. The subquery is pushed into <code>RemoteQueryExpression</code> ,
 * which indicates that given subquery might be executed at remote node by
 * utilizing  the <code>sendQuery</code> method from LinkManager class.
 * 
 * 
 * @author janek
 * 
 */
/**
 * @author janek
 *
 */
public class DistributedQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer {
	private ASTNode node;
	private DBModule module;
	
	private ArrayList<NameExpression> parmDependentNames;

	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException
	{
		node = query;
		this.module = module;
		parmDependentNames = null;

		SBQLTypeChecker checker = new SBQLTypeChecker(module);
		query = checker.typecheckAdHocQuery(node);

		node.accept(this, null);

		changeDependentNames(node);

		return node;
	}

	private void changeDependentNames(ASTNode node2) throws SBQLException
	{
		ASTDependentNameChanger nameChanger = new ASTDependentNameChanger(parmDependentNames);
		
		node2.accept(nameChanger, null);		
	}

	public void reset() {
	}

	public void setStaticEval(ASTAdapter staticEval) {
	}

	@Override
	public Object visitProcedureCallExpression(ProcedureCallExpression expr, Object attr) throws SBQLException {
		return null;
	}

	@Override
	protected Object commonVisitBinaryExpression(BinaryExpression expr, Object attr) throws SBQLException {
		
		if (tryPushRemoteQueryExpression(expr))
			return null;
		else
			return super.commonVisitBinaryExpression(expr, attr);
	}

	@Override
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr) throws SBQLException
	{
		if (expr instanceof RemoteQueryExpression)
			return null;
		else if (tryPushRemoteQueryExpression(expr))
			return null;
		else
			return super.commonVisitUnaryExpression(expr, attr);
	}

	private boolean tryPushRemoteQueryExpression(Expression expr) throws  SBQLException
	{

		if ((expr instanceof NonAlgebraicExpression) && (isDecoratedWithSingleLink(expr)))
		{
			OID oid = expr.links.toArray(new OID[1])[0];

			ASTDistributivityDecorationChecker distchecker = new ASTDistributivityDecorationChecker(oid);
			expr.accept(distchecker, null);

			if (distchecker.isSingleLinkDecorated())
			{
				RemoteQueryExpression remoteExp = pushRemoteQueryExpression(expr);
				if (!checkParmDependency(expr))
				{
					remoteExp.setParmDependent(false);
				}
				else
				{
					remoteExp.setParmDependent(true);
					remoteExp.setParmDependentNames(this.parmDependentNames);
				}
				
				Expression linkExpr = removeLinkNode(remoteExp);
				remoteExp.setSignature( linkExpr.getSignature() );
				
				return true;
			}
		}

		return false;
	}

	private boolean checkParmDependency(Expression expr) throws SBQLException
	{
		if (expr instanceof NonAlgebraicExpression)
		{
			NonAlgebraicExpression nonAlg = (NonAlgebraicExpression) expr;

			DistributedInDependencyChecker ind = new DistributedInDependencyChecker(nonAlg);
			expr.accept(ind, null);

			if (ind.isParmDependent())
			{
				parmDependentNames = new ArrayList<NameExpression>();
				parmDependentNames = ind.getParmDependentNames();

				return true;
			}
		}
		return false;
	}
	
	/**
	 * Pushes the RemoteQueryExpression into the AST query graph.
	 * 
	 * @param e
	 * @return
	 * @throws DatabaseException
	 */
	private RemoteQueryExpression pushRemoteQueryExpression(Expression e) throws SBQLException {
		
		Expression parent = e.getParentExpression();

		RemoteQueryExpression remoteExp = new RemoteQueryExpression(e);		
		remoteExp.setRemoteLink(getLink(e));

		if (parent != null)
			parent.replaceSubexpr(e, remoteExp);

		if (e == this.node)
			this.node = remoteExp;

		return remoteExp;
	}

	/**
	 * Determines if the given Expression is distributed and marked with single
	 * link name different then localhost.
	 * 
	 * @param e
	 * @return
	 * @throws DatabaseException
	 */
	private boolean isDecoratedWithSingleLink(Expression expr) 
	{
		if (expr.links.size() == 1)
		{
			OID oid = expr.links.toArray(new OID[1])[0];
			try {
			    if (!oid.getObjectName().equals(Names.namesstr[Names.LOCALHOST_LINK]))
			    {
			    	return true;
			    }
			} catch (DatabaseException e) {
			    throw new OptimizationException(e, expr,this);
			}
		}

		return false;
	}

	
	/**
	 * Retrieves the link from the AST
	 * 
	 * @param expr - Expression
	 * @return
	 * @throws DatabaseException
	 */
	private OID getLink(Expression expr) 
	{
		OID link = null;

		if (isDecoratedWithSingleLink(expr))
			link = expr.links.toArray(new OID[1])[0];

		return link;
	}


	
	/**
	 * Removes the link node from the AST query graph.
	 * 
	 * @param remoteExp
	 * @return
	 * @throws Exception
	 */
	private Expression removeLinkNode(RemoteQueryExpression remoteExp) throws SBQLException
	{

		ASTLinkFinder finder;
		try {
		    finder = new ASTLinkFinder(remoteExp.getRemoteLink().getObjectName());
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, remoteExp,this);
		}
		remoteExp.accept(finder, null);

		Expression linkExpressin = finder.getLinkExpression();
		Expression linkParent = linkExpressin.getParentExpression();

		if ((linkParent instanceof DotExpression) && (linkParent.getParentExpression() == remoteExp))
		{
			BinaryExpression binary = (BinaryExpression) linkParent;
			remoteExp.setExpression(binary.getRightExpression());
		}
		else if ((linkParent instanceof DotExpression) && (linkParent.getParentExpression() != remoteExp)
				&& (linkParent.getParentExpression() instanceof BinaryExpression))
		{
			BinaryExpression binary = (BinaryExpression) linkParent.getParentExpression();
			binary.setLeftExpression(((BinaryExpression) linkParent).getRightExpression());
		}
		
		return linkExpressin;
	}
}
