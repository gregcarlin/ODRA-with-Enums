package odra.sbql.optimizers.queryrewrite.unionquery;

import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.DeepCopyAST;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.typechecker.SBQLTypeChecker;
import odra.system.config.ConfigDebug;

/**
 * SBQLUnionDistributiveOptimizer This class performs optimization based on
 * distributivity property.
 * 
 * @author murlewski
 */

public class SBQLUnionDistributiveOptimizer extends TraversingASTAdapter
		implements ISBQLOptimizer {

	private ASTNode root;
	private ASTAdapter bindingLevelAdapter;
	private boolean wasUnion = false;

	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException {

		if (ConfigDebug.ASSERTS)
			assert query != null : "query == null";

		this.root = query;
		this.root.accept(this, null);
		
		SBQLTypeChecker typeChecker = new SBQLTypeChecker(module);
		root = typeChecker.typecheckAdHocQuery(root);	

		return root;
	}

	public void reset() {
		wasUnion = false;

	}

	public void setStaticEval(ASTAdapter staticEval) {
		this.bindingLevelAdapter = staticEval;

	}

	/**
	 * @return optimized AST tree
	 */
	public ASTNode getOptimizedQuery() {
		return root;
	}

	/**
	 * @param expr -
	 *            non-algebraic AST node
	 * @param attr
	 * @throws Exception
	 */
	private void applyUnionDistributiveMethod(NonAlgebraicExpression expr,
			Object attr) throws SBQLException {

		if (isDistributive(expr) && (expr.getLeftExpression() instanceof UnionExpression)) {
			DependencyChecker checker = new DependencyChecker(expr);
			expr.getRightExpression().accept(checker, attr);

			if (checker.isDependent) {
				// switch nodes
				Expression localroot = expr.getLeftExpression();
				replaceNodes(expr, (UnionExpression) expr.getLeftExpression());

				// determine the new scope number and binding levels for the new
				// form of a whole query
				this.root.accept(this.bindingLevelAdapter, null);

				if (isDistributive(localroot.getParentExpression())) {
					// aplly this method on parent
					localroot.getParentExpression().accept(this, attr);
				} else {
					// aplly this method on both childs
					localroot.accept(this, attr);
				}
				return;
			}
		}
		// otherwise apply this method on child's expressions
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);

	}

	private void replaceNodes(NonAlgebraicExpression distExpr,
			UnionExpression unionExpr) throws SBQLException {
		
		
		NonAlgebraicExpression copyOfNonAlgebraic = (NonAlgebraicExpression)DeepCopyAST.copy(distExpr);//(NonAlgebraicExpression) distExpr.accept(deepCopy, null);

		if ((distExpr.getParentExpression() != null) && (distExpr.getParentExpression() instanceof BinaryExpression))
		{
			((BinaryExpression) distExpr.getParentExpression()).setLeftExpression(unionExpr);
		}
		else if ((distExpr.getParentExpression() != null) && (distExpr.getParentExpression() instanceof UnaryExpression))
		{
			((UnaryExpression) distExpr.getParentExpression()).setExpression(unionExpr);
		}

		unionExpr.setParentExpression(distExpr.getParentExpression());

		distExpr.setLeftExpression(unionExpr.getLeftExpression());
		copyOfNonAlgebraic.setLeftExpression(unionExpr.getRightExpression());

		unionExpr.getLeftExpression().setParentExpression(distExpr);
		unionExpr.getRightExpression().setParentExpression(copyOfNonAlgebraic);

		unionExpr.setLeftExpression(distExpr);
		unionExpr.setRightExpression(copyOfNonAlgebraic);

		distExpr.setParentExpression(unionExpr);
		copyOfNonAlgebraic.setParentExpression(unionExpr);

		if (this.root == distExpr)
		{
			this.root = unionExpr;
		}

	}

	/**
	 * Check if the operator is distributive
	 * 
	 * @param expr
	 *            AST node representing the operator
	 * @return true if distributive, false otherwise
	 */
	private boolean isDistributive(Expression expr) {
		// TODO add rest of distributive operators
		if ((expr instanceof JoinExpression) || (expr instanceof DotExpression)
				|| (expr instanceof WhereExpression)) {
			return true;
		}

		return false;
	}

	// TODO override rest of distributive operators
	@Override
	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {
		applyUnionDistributiveMethod((NonAlgebraicExpression) expr, attr);
		return null;
	}

	@Override
	public Object visitDotExpression(DotExpression expr, Object attr)
			throws SBQLException {
		applyUnionDistributiveMethod((NonAlgebraicExpression) expr, attr);
		return null;
	}

	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {
		applyUnionDistributiveMethod((NonAlgebraicExpression) expr, attr);
		return null;
	}

}
