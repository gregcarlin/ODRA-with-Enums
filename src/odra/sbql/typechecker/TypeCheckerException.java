package odra.sbql.typechecker;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTAdapter;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

public class TypeCheckerException extends SBQLException {

    /**
     * @param msg
     * @param node
     * @param visitor
     */
    public TypeCheckerException(String msg, ASTNode node, ASTVisitor visitor) {
	super(msg, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param ex
     * @param node
     * @param visitor
     */
    public TypeCheckerException(String msg, Throwable ex,
	    ASTNode node, ASTVisitor visitor) {
	super(msg, ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     */
    public TypeCheckerException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     * @param node
     * @param visitor
     */
    public TypeCheckerException(Throwable ex, ASTNode node, ASTVisitor visitor) {
	super(ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     */
    public TypeCheckerException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param ex
     */
    public TypeCheckerException(String msg, Throwable ex) {
	super(msg, ex);
	// TODO Auto-generated constructor stub
    }

    
   

}