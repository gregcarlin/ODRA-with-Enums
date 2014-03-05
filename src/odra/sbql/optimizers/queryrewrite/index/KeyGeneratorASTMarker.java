package odra.sbql.optimizers.queryrewrite.index;

import java.util.HashMap;
import java.util.Map;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.Expression;

/**
 * Visitor that marks all subNodes keyGenerator attribute with attr Expression
 * Used to associate nodes of index creating query with appropriate key or nonkey  
 * 
 * @author tkowals
 * @version 1.0
 */
class KeyGeneratorASTMarker extends TraversingASTAdapter {

	private HashMap<Expression, Expression> keygens = new HashMap<Expression, Expression>();
	
	protected Object commonVisitExpression(Expression expr, Object attr) throws SBQLException {
		keygens.put(expr, (Expression) attr);
		return super.commonVisitExpression(expr, attr);
	}
	
	public void markIndexSubAST(Expression expr) throws SBQLException {
		expr.accept(this, expr);
	}
	
	public Map<Expression, Expression> getGeneratorsMap(){
		return keygens;
	}
	
}
