package odra.sbql.ast.statements;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;

public class ExpressionStatement extends Statement {

    private Expression E;

    public ExpressionStatement(Expression e) {
	E = e;
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitExpressionStatement(this, attr);
    }

    /**
     * during compilation statement expression can be modified through adding
     * new parent expressions (e.g. dereference) we need to re-clip the
     * statement expresion to the root
     */
    public void fixUpExpression() {
	while (E.getParentExpression() != null) {
	    E = E.getParentExpression();
	}
    }

    public final Expression getExpression() {
	return this.E;
    }

    /**
     * @param e
     *                the e to set
     */
    public void setExpression(Expression e) {
	E = e;
    }
}