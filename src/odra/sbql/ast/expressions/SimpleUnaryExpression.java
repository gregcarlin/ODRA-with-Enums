package odra.sbql.ast.expressions;

import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Operator;

public class SimpleUnaryExpression extends UnaryExpression {
	public Operator O; // taken from the parser
	public transient OID operator; // a concrete operator, set by the typechecker
	
	public SimpleUnaryExpression(Expression e, Operator o) {
		super(e);

		O = o;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitSimpleUnaryExpression(this, attr);
	}
}
