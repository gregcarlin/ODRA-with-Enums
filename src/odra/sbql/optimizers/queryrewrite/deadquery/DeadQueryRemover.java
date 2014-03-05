package odra.sbql.optimizers.queryrewrite.deadquery;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.system.config.ConfigDebug;

/**
 * DeadQueryRemover
 * removes sub-queries that are marked as dead 
 * @author radamus
 *last modified: 2006-12-10
 *@version 1.0
 */
class DeadQueryRemover extends TraversingASTAdapter {

	private boolean removedFlag = false;
	
	public boolean isRemovedFlag() {
		return removedFlag;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitCommaExpression(odra.sbql.ast.expressions.CommaExpression, java.lang.Object)
	 */
	@Override
	public Object visitCommaExpression(CommaExpression expr, Object attr) throws SBQLException {
		remove(expr, attr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.optimizers.OptimizerASTAdapter#visitJoinExpression(odra.sbql.ast.expressions.JoinExpression, java.lang.Object)
	 */
	@Override
	public Object visitJoinExpression(JoinExpression expr, Object attr) throws SBQLException {
		remove(expr, attr);
		return null;
	}
		
	

	private void remove(BinaryExpression expr, Object attr) {
		if(ConfigDebug.ASSERTS) assert !(!expr.getLeftExpression().isMarked() && !expr.getRightExpression().isMarked()) : "both sub-expressions are death? " + expr.toString();
		if(!expr.getLeftExpression().isMarked()){
			
			//remove only when the cardinality == 1
			if(expr.getLeftExpression().getSignature().getMinCard() == 1 && expr.getLeftExpression().getSignature().getMaxCard() == 1){
				expr.getParentExpression().replaceSubexpr(expr, expr.getRightExpression());
				expr.setParentExpression(null);
				removedFlag = true;
			}
		}else expr.getLeftExpression().accept(this, attr);
		if(!expr.getRightExpression().isMarked()){
//			remove only when the cardinality == 1
			if(expr.getRightExpression().getSignature().getMinCard() == 1 && expr.getRightExpression().getSignature().getMaxCard() == 1){
				expr.getParentExpression().replaceSubexpr(expr, expr.getLeftExpression());
				expr.setParentExpression(null);
				removedFlag = true;
			}
		}else expr.getRightExpression().accept(this, attr);
	}
}
