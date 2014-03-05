package odra.sbql.optimizers;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

public class OptimizationException extends SBQLException {
	private final static long serialVersionUID = 20061211L;

	/**
	 * @param msg
	 * @param node
	 * @param visitor
	 */
	public OptimizationException(String msg, ASTNode node,
		ASTVisitor visitor) {
	    super(msg, node, visitor);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 * @param ex
	 * @param node
	 * @param visitor
	 */
	public OptimizationException(String msg, Throwable ex, ASTNode node,
		ASTVisitor visitor) {
	    super(msg, ex, node, visitor);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * @param ex
	 * @param node
	 * @param visitor
	 */
	public OptimizationException(Throwable ex, ASTNode node,
		ASTVisitor visitor) {
	    super(ex, node, visitor);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 * @param ex
	 */
	public OptimizationException(String msg, Throwable ex) {
	    super(msg, ex);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * @param ex
	 */
	public OptimizationException(Throwable ex) {
	    super(ex);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 */
	public OptimizationException(String msg) {
	    super(msg);
	    // TODO Auto-generated constructor stub
	}

	

	

	
}
