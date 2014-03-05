package odra.sbql.ast.declarations;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;

/**
 * @author ks
 * 
 */
public class ViewFieldDeclaration extends SingleFieldDeclaration {
	private ViewDeclaration vd;

	/**
	 * <p>
	 * <u>(non-Javadoc)</u>
	 * 
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getName()
	 */
	@Override
	public String getName() {
		return this.vd.getViewName();
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.declarations.SingleFieldDeclaration#getDeclaration()
	 */
	@Override
	public Declaration getDeclaration() {
	    return getViewDeclaration();
	}

	public ViewFieldDeclaration(ViewDeclaration vb) {
		this.vd = vb;
	}

	

	/**
	 * @return the vd
	 */
	public ViewDeclaration getViewDeclaration() {
	    return vd;
	}

	/**
	 * <p>
	 * <u>(non-Javadoc)</u>
	 * 
	 * @see odra.sbql.ast.ASTNode#accept(odra.sbql.ast.ASTVisitor,
	 *      java.lang.Object)
	 */
	@Override
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitViewFieldDeclaration(this, attr);
	}

	/**
	 * <p>
	 * <u>(non-Javadoc)</u>
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return vd.toString();
	}

}
