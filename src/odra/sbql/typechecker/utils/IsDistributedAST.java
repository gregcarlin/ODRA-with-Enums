package odra.sbql.typechecker.utils;

import java.util.HashSet;
import java.util.Set;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.SBQLException;
import odra.sbql.ast.TraversingASTAdapter;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.optimizers.OptimizationException;
import odra.system.Names;

/**
 * Checks if AST node contains links.
 * 
 * @author janek
 *
 */
public class IsDistributedAST extends TraversingASTAdapter
{
	Set<OID> links = new HashSet<OID>();

	private boolean isDistributed = false;

	public IsDistributedAST()
	{
	}

	@Override
	public Object visitNameExpression(NameExpression expr, Object attr) throws SBQLException
	{
			try {
			    for(OID oid : expr.links)
			    {
			    	if (!oid.getObjectName().equals(Names.namesstr[Names.LOCALHOST_LINK]))
			    	{
			    		isDistributed = true;
			    		links.add( oid );
			    	}					
			    }
			} catch (DatabaseException e) {
			    throw new OptimizationException(e, expr, this);
			}		

		return null;
	}

	public boolean isDistributed()
	{
		return isDistributed;
	}

	public Set<OID> getLinks()
	{
		return links;
	}
}
