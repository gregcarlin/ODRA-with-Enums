package odra.sbql.ast.expressions;

import java.util.ArrayList;

import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTVisitor;

/**
 * @author janek
 * 
 */
public class RemoteQueryExpression extends UnaryExpression
{	
	private OID remoteLink;
	
	private boolean isParmDependent = false;
	private ArrayList<NameExpression> parmDependentNames;
	
	private boolean asynchronous = false;
	
	public RemoteQueryExpression(Expression e)
	{
		super(e);
		
		parmDependentNames = new ArrayList<NameExpression>();
	}
	
	public Object accept(ASTVisitor vis, Object attr) throws SBQLException {
		return vis.visitRemoteQueryExpression(this, attr);
	}
	
	public String getRemoteQueryAsString() throws SBQLException{
				
	    return AST2TextQueryDumper.AST2Text(this.getExpression());
	}

	public OID getRemoteLink() {
		return remoteLink;
	}

	public void setRemoteLink(OID remoteLink) {
		this.remoteLink = remoteLink;
	}

	public ArrayList<NameExpression> getParmDependentNames()
	{
		return parmDependentNames;
	}

	public void setParmDependentNames(ArrayList<NameExpression> dependentNames)
	{
		this.parmDependentNames.addAll(dependentNames);
	}

	public boolean isParmDependent()
	{
		return isParmDependent;
	}

	public void setParmDependent(boolean isDependent)
	{
		this.isParmDependent = isDependent;
	}

	public void runAsynchronously(){
		asynchronous = true;
	}
	
	public boolean isAsynchronous(){
		return asynchronous;
	}
	
}
