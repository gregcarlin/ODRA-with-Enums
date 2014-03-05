package odra.sbql.ast.statements;

import java.util.LinkedList;
import java.util.List;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.system.config.ConfigDebug;
import odra.transactions.ast.ITransactionCapableASTNode;

public final class BlockStatement extends Statement {

	
	private transient BlockInfo bi;

	private Statement S;

	public BlockStatement(Statement s) {
		this.S = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odra.sbql.ast.statements.Statement#flatten()
	 */
	@Override
	public Statement[] flatten() {
		return getStatement().flatten();
	}

	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitBlockStatement(this, attr);
	}

	public boolean hasBlockInfo() {
		return bi != null;
	}

	public void setBlockName(String name) {
		if (bi == null) {
			bi = new BlockInfo();
		}
		bi.blockName = name;
	}

	public String getBlockName() {
		if (bi == null) {
			return null;
		}
		return bi.blockName;
	}

	public void sethasLocalData(boolean hasLocalData) {
		if (ConfigDebug.ASSERTS) {
			assert bi != null : "block name must be set first";
		}
		bi.hasLocalData = hasLocalData;
	}

	public boolean hasLocalData() {
		if (ConfigDebug.ASSERTS) {
			assert bi != null : "there is no block info";
		}
		return bi.hasLocalData;
	}

	// used to trace block names to load local metadata
	// used only in conjuncion with typecheker
	private static class BlockInfo {
		// true if local variables are created in this block
		// for emiter/runtime optimization used only in conjuncion with
		// typechecker
		transient boolean hasLocalData = false;

		transient String blockName;
	}

	public final Statement getStatement() {
		return this.S;
	}

	public List<ITransactionCapableASTNode> getTransactionCapableChildrenASTNodes() {
		List<ITransactionCapableASTNode> children = new LinkedList<ITransactionCapableASTNode>();
		children.add(this.getStatement());
		return children;
	}

}