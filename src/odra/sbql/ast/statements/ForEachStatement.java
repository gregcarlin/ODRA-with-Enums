package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.expressions.Expression;
import odra.transactions.ast.ITransactionCapableASTNode;

public final class ForEachStatement extends Statement {
    private Statement S;

    private Expression E;

    public ForEachStatement(Expression e, Statement s) {
	this.E = e;
	this.S = s;
    }

    public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
	return vis.visitForEachStatement(this, attr);
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

    public Statement getStatement() {
	return this.S;
    }

    public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
	List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
	children.add(this.getStatement());
	return children;
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