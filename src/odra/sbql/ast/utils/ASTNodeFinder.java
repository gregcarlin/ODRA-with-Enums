package odra.sbql.ast.utils;

import java.util.Vector;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.utils.patterns.Pattern;

/**
 * Visitor that searches AST for nodes of given type 
 * and stores it in order they were found
 * 
 * @author tkowals
 */

public class ASTNodeFinder extends TraversingASTAdapter {

	protected Pattern pattern;
	protected Vector<ASTNode> result = new Vector<ASTNode>(); 
	
	boolean skipTraversOnMatch;
	
	public ASTNodeFinder(Pattern pattern, boolean skipTraversOnMatch) {
		this.pattern = pattern;
		this.skipTraversOnMatch = skipTraversOnMatch;
	}
	
	public Vector<ASTNode> findNodes(ASTNode root) throws SBQLException {
		root.accept(this, null);
		return result;
	}
	
	public Vector<ASTNode> getResult() {
		return result;
	}
	
	public void resetFinder() {
		result = new Vector<ASTNode>();
	}
	
	public int countResults() {
		return result.size();
	}
	
	public Object[] getResultAsArray(Object[] array) {
		return result.toArray(array);
	}
	
	protected Object commonVisitStatement(Statement stmt, Object attr)
			throws SBQLException {
		if (pattern.matches(stmt)) {
			result.add(stmt);
			return !skipTraversOnMatch;
		}
		return true;
	}

	protected Object commonVisitExpression(Expression expr, Object attr)
	throws SBQLException {
		if (pattern.matches(expr)) {
			result.add(expr);
			return !skipTraversOnMatch;
		}
		return true;
	}

	protected Object commonVisitUnaryExpression(UnaryExpression expr,
			Object attr) throws SBQLException {		
		if ((Boolean) commonVisitExpression(expr, attr))
			expr.getExpression().accept(this, attr);
		return null;
	}

	protected Object commonVisitBinaryExpression(BinaryExpression expr,
			Object attr) throws SBQLException {
		if (pattern.matches(expr)) {
			if (skipTraversOnMatch) {
				result.add(expr);
				return null;
			} 
			expr.getLeftExpression().accept(this, attr);
			result.add(expr);
			expr.getRightExpression().accept(this, attr);
			return null;
		}
		
		expr.getLeftExpression().accept(this, attr);
		expr.getRightExpression().accept(this, attr);
		
		return null;
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr,
			Object attr) throws SBQLException {
		if ((Boolean) commonVisitExpression(expr, attr)) {
			expr.getConditionExpression().accept(this, attr);
			expr.getThenExpression().accept(this, attr);
			expr.getElseExpression().accept(this, attr);
		}
		return null; 
	}

	public Object visitIfThenExpression(IfThenExpression expr,
			Object attr) throws SBQLException {
		if ((Boolean) commonVisitExpression(expr, attr)) {
			expr.getConditionExpression().accept(this, attr);
			expr.getThenExpression().accept(this, attr);
		}
		return null; 
	}
	
	public Object visitProcedureCallExpression(ProcedureCallExpression expr,
			Object attr) throws SBQLException { 
		if ((Boolean) commonVisitExpression(expr, attr)) {
			expr.getProcedureSelectorExpression().accept(this, attr);
			expr.getArgumentsExpression().accept(this, attr);		
		} 
		return null;
	}

	public Object visitSequentialExpression(SequentialExpression expr,
			Object attr) throws SBQLException {
		if ((Boolean) commonVisitExpression(expr, attr)) {
			expr.getFirstExpression().accept(this, attr);
			expr.getSecondExpression().accept(this, attr);
		}
		return null;
	}
	
	public Object visitBlockStatement(BlockStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr))
			stmt.getStatement().accept(this, attr);
		return null;
	}

	public Object visitExpressionStatement(ExpressionStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr))
			stmt.getExpression().accept(this, attr);
		return null;
	}
	
	public Object visitForEachStatement(ForEachStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr)) {
			stmt.getExpression().accept(this, attr);
			stmt.getStatement().accept(this, attr);
		}
		return null;
	}

	public Object visitIfElseStatement(IfElseStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr)) {
			stmt.getExpression().accept(this, attr);
			stmt.getIfStatement().accept(this, attr);
			stmt.getElseStatement().accept(this, attr);
		}
		return null;
	}

	public Object visitIfStatement(IfStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr)) {
			stmt.getExpression().accept(this, attr);
			stmt.getStatement().accept(this, attr);
		} 
		return null;
	}

	public Object visitSequentialStatement(SequentialStatement stmt, Object attr)
			throws SBQLException {
		if ((Boolean) commonVisitStatement(stmt, attr)) {
			stmt.getFirstStatement().accept(this, attr);
			stmt.getSecondStatement().accept(this, attr);
		}
		return null;
	}
	
}
