package odra.sbql.ast.expressions;

import java.util.HashSet;
import java.util.Set;

import odra.db.OID;
import odra.sbql.ast.ASTNode;
import odra.sbql.debugger.compiletime.DebugNodeData;
import odra.sbql.debugger.compiletime.NoDebugNodeData;
import odra.sbql.emiter.JulietCode;
import odra.sbql.results.compiletime.Signature;

/**
 * Base class for all AST nodes being expressions.
 * 
 * @author raist modifications: 10.12.06 radamus: isMarked flag added - for DeadQueryRemover 08.06.07 radamus: node
 *         debug data added 08.06.07 radamus: node Juliet code
 */

public abstract class Expression extends ASTNode {


	protected Expression P; // parent expression

	private JulietCode julietCode;

	private DebugNodeData debug = NoDebugNodeData.NODEBUGDATA;

	protected transient Signature sign;

	public transient Set<OID> links = new HashSet<OID>();

	private transient boolean isMarked = false;

	/** a flag determining if this expression is substituted by view rewriting */
	public transient boolean isViewSubstituted = false;
	private Signature substitutedSignature = null;
	
	/**
	 * @return the substitutedSignature or null if not exists
	 */
	public Signature getSubstitutedSignature() {
		return substitutedSignature;
	}

	/**
	 * @param substitutedSignature the substitutedSignature to set
	 */
	public void setSubstitutedSignature(Signature substitutedSignature) {
		this.substitutedSignature = substitutedSignature;
	}

	private transient boolean isEnforced = false;

	/** wrapper module temporary id for wrapper rewriting */
	public transient String wrapper = null;

	public void replaceSubexpr(Expression oldexpr, Expression newexpr) {
		assert false : "replaceSubexpr() on a leaf AST node";
	}

	public Expression[] flatten() {
		return new Expression[] { this };
	}

	public final JulietCode getJulietCode() {
		return this.julietCode;
	}
	
	public final Expression getParentExpression () {
		return this.P;
	}
	
	public final void setParentExpression (Expression p) {
		this.P = p;
	}

	/**
	 * @param sign the sign to set
	 */
	public void setSignature(Signature sign) {
	    this.sign = sign;
	 // set the current node the generator of the signature
	    sign.setOwnerExpression(this);
	}

	/**
	 * @return the sign
	 */
	public Signature getSignature() {
	    return sign;
	}

	/**
	 * @param julietCode the julietCode to set
	 */
	public JulietCode setJulietCode(JulietCode julietCode)
	{
	    return this.julietCode = julietCode;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(DebugNodeData debug)
	{
	    this.debug = debug;
	}

	/**
	 * @return the debug
	 */
	public DebugNodeData getDebug()
	{
	    return debug;
	}

	/**
	 * @return the isMarked
	 */
	public boolean isMarked() {
	    return isMarked;
	}

	/**
	 * @param isMarked the isMarked to set
	 */
	public void setMarked(boolean isMarked) {
	    this.isMarked = isMarked;
	}

	/**
	 * @return the isEnforced
	 */
	public boolean isEnforced() {
		return isEnforced;
	}

	/**
	 * @param isEnforced the isEnforced to set
	 */
	public void setEnforced(boolean isEnforced) {
		this.isEnforced = isEnforced;
	}
	

	    
	
}