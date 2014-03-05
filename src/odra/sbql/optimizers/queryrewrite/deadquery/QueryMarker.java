package odra.sbql.optimizers.queryrewrite.deadquery;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.NameExpression;

/**
 * QueryMarker
 * mark the AST expressions that are used in the calculation
 * of the final result
 * @author radamus
 *last modified: 2006-12-10
 *@version 1.0
 */
class QueryMarker extends TraversingASTAdapter {

	public QueryMarker() {
		// TODO Auto-generated constructor stub
	}

	/*
	     * (non-Javadoc)
	     * 
	     * @see odra.sbql.optimizers.OptimizerASTAdapter#visitNameExpression(odra.sbql.ast.expressions.NameExpression,
	     *      java.lang.Object)
	     */
	    @Override
	    public Object visitNameExpression(NameExpression expr, Object attr)
		    throws SBQLException {

		NameExpression markedexpr = expr;
		markedexpr.setMarked(true);
		// we mark all the expression nodes that are present on the association
		// path
		while (markedexpr.getAssociated() != null) {
		    this.markUpward(markedexpr);

		    markedexpr = markedexpr.getAssociated();
		    markedexpr.setMarked(true);

		}//FIXME this is a patch, it should be re-think
		if(markedexpr.isAuxiliaryName()){
		    Expression tomark = markedexpr.getSignature().getAssociatedExpression();
		    this.markUpward(tomark);
		    tomark.setMarked(true);
		}
		// finally we mark all the nodes up from the last expression in the
		// association path
		this.markUpward(markedexpr);
		return null;
	    }

	    public void markUpward(Expression expr) {
		while (expr.getParentExpression() != null
			&& !expr.getParentExpression().isMarked()) {
		    expr = expr.getParentExpression();
		    expr.setMarked(true);
		}
	    }
}
