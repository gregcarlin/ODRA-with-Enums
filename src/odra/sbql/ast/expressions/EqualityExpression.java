package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Operator;

// FIXME: TK: Is it really necessary (what about SimpleBinaryExpression?) 
public class EqualityExpression extends BinaryExpression {
	public Operator O;
	
	public EqualityExpression(Expression e1, Expression e2, Operator o) {
		super(e1, e2);
		
		O = o;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitEqualityExpression(this, attr);
	}
}
