package odra.sbql.ast.expressions;

import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Operator;

public class SimpleBinaryExpression extends BinaryExpression {
	public Operator O; // taken from the parser
	public transient OID operator; // a concrete operator, set by the typechecker
	
	public SimpleBinaryExpression(Expression e1, Expression e2, Operator o) {
		super(e1, e2);
		
		O = o;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitSimpleBinaryExpression(this, attr);
	}
}
