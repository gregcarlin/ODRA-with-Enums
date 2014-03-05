package odra.sbql.typechecker;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBLink;
import odra.db.objects.meta.MBSchema;
import odra.sbql.results.AbstractQueryResult;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.Signature;
import odra.sbql.stack.Binder;
import odra.sbql.stack.EnvsElement;
import odra.sbql.stack.IBindingGuru;
import odra.sbql.stack.Nester;
import odra.sbql.stack.RemoteNester;
import odra.sbql.stack.StackFrame;
import odra.system.config.ConfigDebug;

/**
 * This class implements the static binding used during the type check. 
 * It defines what happens when the binding hits a particular environment stack frame.
 * It operates on a sinale stack frame and NOT on the whole stack. 
 * 
 * @author raist, stencel
 */

public class StaticBindingManager implements IBindingGuru {
	private StackFrame frame;

	/**
	 * Sets the frame where binding is to be performed.
	 * @param frame is a environment stack frame where binding will be performed. 
	 */
	public void setFrame(StackFrame frame) {
		this.frame = frame;
	}

	/**
	 * Performs the binding in the indicated stack frame.
	 * @param name is the name to be bound.
	 * @return is the collection of bindings, i.e. the references to objects found in the frame.
	 */
	public AbstractQueryResult[] bind(int name_id) throws DatabaseException {		
		Vector<Signature> res = new Vector();
		
		if (ConfigDebug.ASSERTS) assert frame != null;
		
		// look for binders
		Vector<EnvsElement> chain = frame.data.get(name_id);

		if (chain != null) {
			for (EnvsElement elem : chain) {
				Binder bndr = (Binder) elem;
				Signature bndrval = (Signature) bndr.val;
				res.addElement(bndrval);
			}
		}

		// look for nesters
		chain = frame.data.get(StackFrame.NESTER_ID);		
		if (chain != null) {

			for (EnvsElement e : chain) {
				Nester nester = (Nester) e;

				OID ref = nester.val.findFirstChildByNameId(name_id);
				//for(OID o:nester.val.derefComplex())
					//System.out.println(o.getObjectName()+" "+o.toString());
				
				if (ref == null)
					continue;
				
				if (ref.isAggregateObject()) {
					OID[] aggels = ref.derefComplex();
						
					for (OID i : aggels)
					{
						ReferenceSignature rs = new ReferenceSignature(i, true);						
						res.addElement(rs);
					}
				}
				else
				{
					ReferenceSignature rs = new ReferenceSignature(ref, true);
					res.addElement(rs);
				}
			}
		}
		//look for remote nesters
		chain = frame.data.get(StackFrame.RMT_NESTER_ID);
		if(chain != null){
			for (EnvsElement e : chain) {
				RemoteNester rnester = (RemoteNester) e;
				
				OID remoteMetaBase;
				boolean link = new MBLink(rnester.link).isValid();
				if (link) 
					remoteMetaBase = new MBLink(rnester.link).getMetaBase().getMetabaseEntry();
				else 
					remoteMetaBase = new MBSchema(rnester.link).getMetaBase().getMetabaseEntry();
					
				OID ref = remoteMetaBase.findFirstChildByNameId(name_id);
				
				if (ref == null)
					continue;
				
				if (ref.isAggregateObject()) {
					OID[] aggels = ref.derefComplex();
						
					for (OID i : aggels)
					{	
						ReferenceSignature rsig = new ReferenceSignature(i, true);
						if (link) rsig.addLinkDecoration( rnester.link );
						res.addElement(rsig);
					}
				}
				else {
					ReferenceSignature rsig = new ReferenceSignature(ref, true);
					if (link) rsig.addLinkDecoration( rnester.link );
					res.addElement(rsig);
				}

			}
		}
		return res.toArray(new AbstractQueryResult[res.size()]);
	}
}
