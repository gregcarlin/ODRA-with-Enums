package odra.sbql.builder;

import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;



/**
 * Exception class used to signal that during the process of module compilation
 * something went wrong.
 * 
 * @author raist
 */

public class CompilerException extends BuilderException {

    /**
     * @param msg
     */
    public CompilerException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param module
     * @param line
     * @param column
     */
//    public CompilerException(String msg, String module, int line, int column) {
//	super(msg, module, line, column);
//	// TODO Auto-generated constructor stub
//    }

    /**
     * @param msg
     * @param ex
     * @param module
     * @param line
     * @param column
     */
//    public CompilerException(String msg, Throwable ex, String module, int line,
//	    int column) {
//	super(msg, ex, module, line, column);
//	// TODO Auto-generated constructor stub
//    }

    /**
     * @param ex
     * @param module
     * @param line
     * @param column
     */
//    public CompilerException(Throwable ex, String module, int line, int column) {
//	super(ex, module, line, column);
//	// TODO Auto-generated constructor stub
//    }

    /**
     * @param ex
     */
    public CompilerException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param node
     * @param visitor
     */
    public CompilerException(String msg, ASTNode node,
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
    public CompilerException(String msg, Throwable ex,
	    ASTNode node, ASTVisitor visitor) {
	super(msg, ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     * @param node
     * @param visitor
     */
    public CompilerException(Throwable ex, ASTNode node, ASTVisitor visitor) {
	super(ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

  
}