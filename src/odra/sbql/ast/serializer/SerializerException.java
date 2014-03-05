/**
 * 
 */
package odra.sbql.ast.serializer;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

/**
 * SerializerException
 * @author Radek Adamus
 *@since 2007-11-29
 *last modified: 2007-11-29
 *@version 1.0
 */
public class SerializerException extends SBQLException {

    
    

    /**
     * @param ex
     */
    public SerializerException(Throwable ex) {
	super(ex);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param node
     * @param visitor
     */
    public SerializerException(String msg, ASTNode node, ASTVisitor visitor) {
	super(msg, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param ex
     * @param node
     * @param visitor
     */
    public SerializerException(String msg, Throwable ex,
	    ASTNode node, ASTVisitor visitor) {
	super(msg, ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     */
    public SerializerException(String msg) {
	super(msg);
	// TODO Auto-generated constructor stub
    }

    /**
     * @param ex
     * @param node
     * @param visitor
     */
    public SerializerException(Throwable ex, ASTNode node, ASTVisitor visitor) {
	super(ex, node, visitor);
	// TODO Auto-generated constructor stub
    }

   

}
