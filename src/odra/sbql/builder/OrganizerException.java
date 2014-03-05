package odra.sbql.builder;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

/**
 * Exception class used to signal that during the process of module modification
 * something went wrong.
 * 
 * @author raist
 */

public class OrganizerException extends SBQLException {

   public OrganizerException(String msg) {
      super(msg);
   }

   
   public OrganizerException(Throwable ex) {
	super(ex);
}


/**
 * @param msg
 * @param node
 * @param visitor
 */
public OrganizerException(String msg, ASTNode node, ASTVisitor visitor) {
    super(msg, node, visitor);
    // TODO Auto-generated constructor stub
}


/**
 * @param msg
 * @param ex
 * @param node
 * @param visitor
 */
public OrganizerException(String msg, Throwable ex, ASTNode node,
	ASTVisitor visitor) {
    super(msg, ex, node, visitor);
    // TODO Auto-generated constructor stub
}


/**
 * @param msg
 * @param ex
 */
public OrganizerException(String msg, Throwable ex) {
    super(msg, ex);
    // TODO Auto-generated constructor stub
}


/**
 * @param ex
 * @param node
 * @param visitor
 */
public OrganizerException(Throwable ex, ASTNode node, ASTVisitor visitor) {
    super(ex, node, visitor);
    // TODO Auto-generated constructor stub
}
   
}