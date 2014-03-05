package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Operator;
//TODO remove
public class MatchStringExpression extends SimpleBinaryExpression {

	
	public MatchStringExpression(Expression e1, Expression e2, Operator o) {
		super(e1, e2, o);
				
	}
	
	
}
