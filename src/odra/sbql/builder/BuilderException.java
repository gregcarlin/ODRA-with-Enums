package odra.sbql.builder;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

/**
 * Exception class used to signal that during the process of module creation something went wrong.
 * 
 * @author raist
 */

public abstract class BuilderException extends SBQLException {

    
    /**
     * @param msg
     */
    protected BuilderException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }


    /**
     * @param ex
     */
    public BuilderException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param node
     * @param visitor
     */
    public BuilderException(String msg, ASTNode node,
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
    public BuilderException(String msg, Throwable ex,
	    ASTNode node,   ASTVisitor visitor) {
	super(msg, ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     * @param node
     * @param visitor
     */
    public BuilderException(Throwable ex,ASTNode node,
	    ASTVisitor visitor) {
	super(ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    
   
}