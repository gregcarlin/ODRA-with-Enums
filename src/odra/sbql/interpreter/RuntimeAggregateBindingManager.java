/**
 * 
 */
package odra.sbql.interpreter;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.stack.AggregateBinder;
import odra.sbql.stack.Binder;
import odra.sbql.stack.EnvsElement;
import odra.security.UserContext;

/**
 * RuntimeAggregateBindingManager
 * Special purpose binding manager used for intercepting AggregateBinders
 * and return reference to the aggregate object instead of references to 
 * aggregate children
 * @author Radek Adamus
 *@since 2008-07-01
 *last modified: 2008-07-01
 *@version 1.0
 */
public class RuntimeAggregateBindingManager extends RuntimeBindingManager {

	/**
	 * @param usrctx
	 */
	public RuntimeAggregateBindingManager(UserContext usrctx) {
		super(usrctx);		
	}

	/* (non-Javadoc)
	 * @see odra.sbql.interpreter.RuntimeBindingManager#binders2Result(java.util.Vector)
	 */
	@Override
	protected boolean binders2Result(Vector<EnvsElement> chain)
			throws DatabaseException {
		Result res;
		for (EnvsElement e : chain) {
			res = (Result) ((Binder) e).val;
			//we are interested only in binders
			if (e instanceof AggregateBinder) {
				AggregateBinder ab = (AggregateBinder)e;
				if(((ReferenceResult)ab.val).value.getObjectNameId() == ab.name_id){
					bag.addElement((SingleResult)res);
					return true;
				}
			} 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.interpreter.RuntimeBindingManager#OIDs2Result(java.util.Vector)
	 */
	@Override
	protected boolean OIDs2Result(Vector<OID> oidchain)
			throws DatabaseException {
		//does nothing
		return false;
	}
	

}
