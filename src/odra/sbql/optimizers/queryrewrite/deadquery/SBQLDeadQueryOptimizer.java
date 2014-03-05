package odra.sbql.optimizers.queryrewrite.deadquery;

import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.optimizers.ISBQLOptimizer;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.results.compiletime.StructSignature;

/**
 * SBQLDeadQueryOptimizer
 * implements optimization through removing dead queries 
 * @author radamus
 *last modified: 2006-12-10
 *@version 1.0
 */
public class SBQLDeadQueryOptimizer extends TraversingASTAdapter implements ISBQLOptimizer{
	boolean wasMarked = false;
	ASTNode cutPlace;
	ASTNode root;
	private ASTAdapter staticEval;
	
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.ISBQLOptimizer#setStaticEval(odra.sbql.ast.ASTAdapter)
	 */
	public void setStaticEval(ASTAdapter staticEval) {
		this.staticEval = staticEval;
		
	}
	public void reset() {
		wasMarked = false;
		cutPlace = null;
	}
	
	
	public ASTNode optimize(ASTNode query, DBModule module) throws SBQLException{
		this.root = query;
		try {
		    this.setSourceModuleName(module.getName());
		} catch (DatabaseException e) {
		    throw new OptimizationException(e, query,this);
		}
		do {
		    wasMarked = false;
		    query.accept(this, null);
		    if (wasMarked) {
		    	DeadQueryRemover dqr = new DeadQueryRemover();
		    	cutPlace.accept(dqr, null);
				//regenerate signatures
		    	root.accept(staticEval, null);
		    	if (!dqr.isRemovedFlag())
		    		wasMarked = false;
		    	continue;
		    }
		}while (wasMarked);

		return root;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitDotExpression(odra.sbql.ast.expressions.DotExpression, java.lang.Object)
	 */
	@Override
	public Object visitDotExpression(DotExpression expr, Object attr) throws SBQLException {
		markAST(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitForAllExpression(odra.sbql.ast.expressions.ForAllExpression, java.lang.Object)
	 */
	@Override
	public Object visitForAllExpression(ForAllExpression expr, Object attr) throws SBQLException {
		markAST(expr, attr);
		return null;
		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitForSomeExpression(odra.sbql.ast.expressions.ForSomeExpression, java.lang.Object)
	 */
	@Override
	public Object visitForSomeExpression(ForSomeExpression expr, Object attr) throws SBQLException {
		markAST(expr, attr);
		return null;
	}

	private void markAST(NonAlgebraicExpression expr, Object attr) throws SBQLException {
		if(expr.getLeftExpression().getSignature() instanceof StructSignature )
		{
			wasMarked = true;
			cutPlace = expr; 			
			//check right sub-query
			expr.getRightExpression().accept(new QueryMarker(), attr);

		}else {
			expr.getLeftExpression().accept(this, attr);
			expr.getRightExpression().accept(this, attr);
		}
	}

	
}
