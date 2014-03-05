package odra.sbql.interpreter;

import java.util.Vector;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.stack.AggregateBinder;
import odra.sbql.stack.Binder;
import odra.sbql.stack.EnvsElement;
import odra.sbql.stack.IBindingGuru;
import odra.sbql.stack.Nester;
import odra.sbql.stack.RemoteNester;
import odra.sbql.stack.StackFrame;
import odra.security.UserContext;

public class RuntimeBindingManager implements IBindingGuru {
	protected StackFrame frame;

	private UserContext usrctx;

	//auxiliary bag for binding result 
	protected BagResult bag;
	
	public RuntimeBindingManager(UserContext usrctx) {
		this.usrctx = usrctx;
	}

	public void setFrame(StackFrame frame) {
		this.frame = frame;
	}
	
	public AbstractQueryResult[] bind(int name_id) throws DatabaseException {
		bag = new BagResult();
		
		// look for binders
		boolean found = findBinders(name_id);
		
		// look for nesters
		found =	OIDs2Result(nesters2OIDs(name_id)) || found;	
					
		// look for remote nesters
		found = bindRemoteNesters(name_id) || found;		
		
		if (!found)
			return new AbstractQueryResult[0];
		
		return bagToReturnResult(bag);		
	}
	
	/**
	 * Method looks for binders
	 * @param name_id id of bound name 
	 * @return true if appropriate binders where found
	 * @throws DatabaseException
	 */
	protected final boolean findBinders(int name_id) throws DatabaseException {
		Vector<EnvsElement> chain = frame.data.get(name_id);
		if (chain == null)
			return false;
		else {
			return binders2Result(chain);			
		}
	}
	
	/**
	 * Creates binding results from binders
	 * @param chain vector containing found binders 
	 * @throws DatabaseException
	 */
	protected boolean binders2Result(Vector<EnvsElement> chain) throws DatabaseException {		
		
		Result res;
		for (EnvsElement e : chain) {
			res = (Result) ((Binder) e).val;
			if (e instanceof AggregateBinder) {
				ReferenceResult rres = (ReferenceResult) res;
				for (OID id : rres.value.derefComplex()) 
					bag.addElement(new ReferenceResult(id, rres.parent));				
			} else {
				for (SingleResult r : res.elementsToArray())
					bag.addElement(r);
			}
		}
		return true;
	}
	
	/**
	 * Auxiliary method returning vector of OIDs bound through nesters  
	 * @param name_id id of bound name 
	 * @return vector of bound objects OIDs
	 * @throws DatabaseException
	 */
	protected final Vector<OID> nesters2OIDs(int name_id) throws DatabaseException {

		Vector<OID> oidchain = new Vector<OID>();
		
		Vector<EnvsElement> chain = frame.data.get(StackFrame.NESTER_ID);
		if (chain != null)
			for (EnvsElement e : chain ) {
				Nester nester = (Nester) e;
	
				OID res = nester.val.findFirstChildByNameId(name_id);
	
				if (res == null)
					continue;		
				
				oidchain.add(res);
			}
		
		return oidchain;
	}
		
	/**
	 * Creates binding results from OIDs
	 * @param oidchain vector containing found OIDs 
	 * @return true if any result was created
	 * @throws DatabaseException
	 */
	protected boolean OIDs2Result(Vector<OID> oidchain) throws DatabaseException {	
		if (oidchain.size() == 0)
			return false;
		
		for (OID res : oidchain)
			if (res.isAggregateObject()) 
				for (OID i : res.derefComplex())
					bag.addElement(new ReferenceResult(i));
			else
				bag.addElement(new ReferenceResult(res));
		
		return true;
	}
	
	/**
	 * Method performing binding through remote nesters
	 * @param name_id id of bound name 
	 * @return true if binding was successful
	 */
	protected final boolean bindRemoteNesters(int name_id){
		Vector<EnvsElement> chain = frame.data.get(StackFrame.RMT_NESTER_ID);
		
		if (chain != null) {	
			for (EnvsElement e : chain) {
				RemoteNester nester = (RemoteNester) e;
	
				try {
					Result res = LinkManager.getInstance().remoteBind(
							new DBLink(nester.link),
							Database.getNameIndex().id2name(name_id), usrctx);
					if (res.elementsCount() > 0)
						bag.addAll(res);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}		
		}
		
		return (bag.elementsCount() > 0);
			
	}
	
	protected final AbstractQueryResult[] bagToReturnResult(BagResult bag) {
		if (bag.elementsCount() == 1)
			return new AbstractQueryResult[] { bag.elementAt(0) };
		else
			return new AbstractQueryResult[] { bag };		
	}
	
}

