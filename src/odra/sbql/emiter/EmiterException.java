/**
 * 
 */
package odra.sbql.emiter;

import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.builder.CompilerException;

/**
 * EmiterException
 * @author Radek Adamus
 *@since 2007-11-29
 *last modified: 2007-11-29
 *@version 1.0
 */
public class EmiterException extends CompilerException {

    /**
     * @param msg
     * @param node
     * @param visitor
     */
    public EmiterException(String msg, ASTNode node, ASTVisitor visitor) {
	super(msg, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param ex
     * @param node
     * @param visitor
     */
    public EmiterException(String msg, Throwable ex,
	    ASTNode node, ASTVisitor visitor) {
	super(msg, ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     */
    public EmiterException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     * @param node
     * @param visitor
     */
    public EmiterException(Throwable ex, ASTNode node, ASTVisitor visitor) {
	super(ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     */
    public EmiterException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

   
}
