package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;

public class CreateTemporalExpression extends CreateExpression {

	public CreateTemporalExpression(Name n, Expression e) {
		super(n, e);
		// TODO Auto-generated constructor stub
	}
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitCreateTemporalExpression(this, attr);
	}
}
