package odra.sbql.optimizers.queryrewrite.index;

import java.util.HashMap;
import java.util.Map;

import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;

/**
 * Visitor that is used to check:<br>
 * <ul>
 * <li>if given key AST is associated non-key generator expression.  
 * Another words if key is generated direcly from nonkey value objects.</li>
 * <li>if given AST (e.g. taken from predicate condition) 
 * is associated direcly with database root.</li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
class ASTBoundToChecker extends TraversingASTAdapter {

	private Expression boundToExpr;
	
	private Map<Expression, Expression> keygens;
	private HashMap<Expression, Expression> keyasss = new HashMap<Expression, Expression>();
	
	public ASTBoundToChecker(Map<Expression, Expression> keygens) {
		this.keygens = keygens;
	}

	public void checkASTBoundTo(Expression expr, Expression boundToExpr) throws SBQLException {
		this.boundToExpr = boundToExpr;
		expr.accept(this, expr);
	}
	
	protected Object commonVisitUnaryExpression(UnaryExpression expr, Object attr)
		throws SBQLException {
		if (expr == boundToExpr) return expr;
		if (keygens.get(expr) != attr)
			throw new IndexOptimizerException("Key value must generated from indexed objects", null);
		if (keyasss.containsKey(expr)) return keyasss.get(expr);
		if (expr.getSignature().getAssociatedExpression() == boundToExpr) {
			keyasss.put(expr, boundToExpr);
			return boundToExpr;
		}
		if (expr.getSignature().getAssociatedExpression() != null)
			keyasss.put(expr, (Expression) expr.getSignature().getAssociatedExpression().accept(this, attr));
		expr.getExpression().accept(this, attr);
		return keyasss.get(expr);
	}
	
	protected Object commonVisitBinaryExpression(BinaryExpression expr, Object attr)
			throws SBQLException {
		if (expr == boundToExpr) return expr;
		if (keygens.get(expr) != attr)
			throw new IndexOptimizerException("Key value must generated from indexed objects", null);
		if (keyasss.containsKey(expr)) return keyasss.get(expr);
		if (expr.getSignature().getAssociatedExpression() == boundToExpr) {
			keyasss.put(expr, boundToExpr);
			return boundToExpr;
		}
		if (expr.getSignature().getAssociatedExpression() != null)
			keyasss.put(expr, (Expression) expr.getSignature().getAssociatedExpression().accept(this, attr));
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		return keyasss.get(expr);
	}
	
	protected Object commonVisitLiteral(Expression expr, Object attr) throws SBQLException { 
		return null;
	}

	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException {
		if (expr == boundToExpr) return expr;
		if (!keygens.containsKey(expr) || (keygens.get(expr) != attr)) 
			throw new IndexOptimizerException("Key value must generated from indexed objects", null);
		if (keyasss.containsKey(expr)) return keyasss.get(expr);
		if (expr.getSignature().getAssociatedExpression() == boundToExpr) {
			keyasss.put(expr, boundToExpr);
			return boundToExpr;
		}
		if ((expr.getSignature().getAssociatedExpression() == null))// && (attr != boundToExpr) && (expr != boundToExpr))
			throw new IndexOptimizerException("Key value must generated from indexed objects", null);
		keyasss.put(expr, (Expression) expr.getSignature().getAssociatedExpression().accept(this, attr));
		return super.visitNameExpression(expr, attr);
	}
	
	public Object visitAssignExpression(AssignExpression expr, Object attr)
			throws SBQLException {
			throw new IndexOptimizerException(expr.getClass().getName() + " is banned for indexing", null);
	}

	public Object visitEmptyStatement(EmptyStatement stmt, Object attr)
			throws SBQLException {
		throw new IndexOptimizerException(stmt.getClass().getName() + " is banned for indexing", null);
	}
	
	public Object visitJoinExpression(JoinExpression expr, Object attr)
			throws SBQLException {
		if (keygens.get(expr) == boundToExpr)
			throw new IndexOptimizerException(expr.getClass().getName() + " is banned for indexing as nonkey expression", null);
		return super.visitJoinExpression(expr, attr);
	}
	
	public Object visitOrderByExpression(OrderByExpression expr, Object attr)
			throws SBQLException {
		if (keygens.get(expr) == boundToExpr)
			throw new IndexOptimizerException(expr.getClass().getName() + " is banned for indexing as nonkey expression", null);
		return super.visitOrderByExpression(expr, attr);
	}
	
	public Object visitProcedureCallExpression(ProcedureCallExpression expr,
			Object attr) throws SBQLException {
		if (keygens.get(expr) == boundToExpr)
			throw new IndexOptimizerException(expr.getClass().getName() + " is banned for indexing as nonkey expression", null);
		return super.visitProcedureCallExpression(expr, attr);
	}

	public Object visitReturnWithoutValueStatement(
			ReturnWithoutValueStatement stmt, Object attr) throws SBQLException {
		throw new IndexOptimizerException(stmt.getClass().getName() + " is banned for indexing", null);
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt,
			Object attr) throws SBQLException {
		throw new IndexOptimizerException(stmt.getClass().getName() + " is banned for indexing", null);
	}
	

	public Object visitWhereExpression(WhereExpression expr, Object attr)
			throws SBQLException {
		if (keygens.get(expr) == boundToExpr)
			throw new IndexOptimizerException(expr.getClass().getName() + " is banned for indexing as nonkey expression", null);
		return super.visitWhereExpression(expr, attr);
	}
	
}
