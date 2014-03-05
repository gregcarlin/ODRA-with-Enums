package odra.sbql.ast.expressions;

import odra.sbql.SBQLException;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.terminals.Name;
import odra.sbql.stack.BindingInfo;

public class NameExpression extends Expression {
	private Name N;
	private NameExpression associated;
	private boolean isAuxiliary;
	private transient BindingInfo binfo;
	public transient boolean virtualBind;
	
	public NameExpression(Name n) {
		N = n;
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitNameExpression(this, attr);
	}

	/**
	 * @param binfo the binfo to set
	 */
	public void setBindingInfo(BindingInfo binfo)
	{
	    this.binfo = binfo;
	}

	/**
	 * @return the binfo
	 */
	public BindingInfo getBindingInfo()
	{
	    return binfo;
	}


	/**
	 * @return the n
	 */
	public final Name name()
	{
	    return N;
	}

	/**
	 * @return the associated
	 */
	public NameExpression getAssociated() {
	    return associated;
	}

	/**
	 * @param associated the associated to set
	 */
	public void setAssociated(NameExpression associated) {
	    this.associated = associated;
	}

	/**
	 * @return true if the bounded name is auxiliary
	 */
	public boolean isAuxiliaryName() {
	    return isAuxiliary;
	}

	/**
	 * @param isAuxiliary the isAuxiliary to set
	 */
	public void setAuxiliary(boolean isAuxiliary) {
	    this.isAuxiliary = isAuxiliary;
	}

	
}
