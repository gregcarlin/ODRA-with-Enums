package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.transactions.ast.ITransactionCapableASTNode;

public class SequentialStatement extends Statement {


	private Statement S1;

	private Statement S2;

	public SequentialStatement(Statement s1, Statement s2) {
	    	this.S1 = s1;
		this.S2 = s2;
	}

	public Statement[] flatten() {
		Statement[] e1f = getFirstStatement().flatten();
		Statement[] e2f = getSecondStatement().flatten();
		Statement[] seq = new Statement[e1f.length + e2f.length];
		int i;
		for (i = 0; i < e1f.length; i++) {
			seq[i] = e1f[i];
		}
		for (int j = 0; j < e2f.length; j++) {
			seq[i + j] = e2f[j];
		}
		return seq;
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitSequentialStatement(this, attr);
	}

	public final Statement getFirstStatement() {
		return this.S1;
	}

	public final Statement getSecondStatement() {
		return this.S2;
	}

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
		children.add(this.getFirstStatement());
		children.add(this.getSecondStatement());
		return children;
	}

	


}