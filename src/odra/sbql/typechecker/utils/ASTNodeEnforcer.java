/**
 * 
 */
package odra.sbql.typechecker.utils;

import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;

/**
 * NodeEnforcer
 * base class for implementing AST node enforcers that
 * if some customizable conditions are fullfilled inserts specific node
 * kind into the AST 
 * @author Radek Adamus
 *@since 2008-08-22
 *last modified: 2008-08-28
 *@version 1.0
 */
public abstract class ASTNodeEnforcer<Type extends ASTNode> {
	protected final ASTVisitor astVisitor;	
	protected boolean enforcePerformed = false; 
	/**
	 * @param astVisitor the visitor that has to be used to
	 * traverse the AST after node insertion (or null if no traverse is required)
	 */
	protected ASTNodeEnforcer(ASTVisitor astVisitor) {
		this.astVisitor = astVisitor;
			
	}
	
	/**
	 * if the {@link enforceIsRequired} returns true enforce new new 
	 * into the AST referenced by the node
	 * the way the insertion is performed depends on the sub-class 
	 * implementation
	 * @param node - the node that is a reference for enforcing
	 * new node
	 * @param attr
	 * @return enforced node (if node was enforced) or the 
	 * param node (if no action was performed) 
	 */
	public Type enforce(Type node, Object attr){
		assert node != null: "node != null";
		this.enforcePerformed = false;
		if(enforceIsRequired(node)){
			Type enforcedNode = enforceInternal(node);
			enforcedNode.column = node.column;
			enforcedNode.line = node .line;
			if(astVisitor != null)
				enforcedNode.accept(astVisitor, attr);
			enforcePerformed = true;
			return enforcedNode;
		}
		return node;
	}
	
	/**
	 * @return true if node was enforced
	 */
	public boolean enforcePerformed(){
		return this.enforcePerformed;
	}
	/**
	 * perform node enforce, called only if enforceIsRequired returns true
	 * @param node - the reference node, that can be used to
	 * @return - enforced node
	 */
	protected abstract Type enforceInternal(Type node);

	/**	  
	 * @param node - the reference node, that can be used to 
	 * check some conditions the enforcing is based on
	 * @return true if enforcing is required, false otherwise
	 */
	protected abstract boolean enforceIsRequired(Type node);
	
}
