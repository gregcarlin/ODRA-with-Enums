package odra.sbql.stack;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigServer;


/**
 * The class represents envs stack frames. Every frame has two elements:
 * data and a dynamic link. Data is composed of pairs <key, {v0, v1, v2, ...}>,
 * which is equivalent to a list of binders key(v0), key(v1), key(v2), ...
 * Apart from binders, envs sections can store nesters (elements supporting
 * lazy binding). Nesters belonging to the same envs frame are stored
 * as one list, under the name $nester. The dynamic link points at
 * an envs section to which the process of binding should proceed
 * when no binders can be found in the current section
 * (remember the picture? some sections are black!).
 * 
 * @author raist
 */

public class StackFrame {
	public Hashtable<Integer, Vector<EnvsElement>> data = new Hashtable<Integer, Vector<EnvsElement>>(); // key and a list of binders or nesters
	public StackFrame ebp; // dynamic link
	
	public StackFrame() {
		ebp = null;
		data.put(NESTER_ID, new Vector<EnvsElement>());
		data.put(RMT_NESTER_ID, new Vector<EnvsElement>());
	}

	public StackFrame(StackFrame callee) {
		ebp = callee;
		data.put(NESTER_ID, new Vector<EnvsElement>());
		data.put(RMT_NESTER_ID, new Vector<EnvsElement>());
	}

	// introduce a new binder in the frame
	public void enter(Binder el) {
		Vector<EnvsElement> chain = data.get(el.name_id);

		if (chain == null) {
			chain = new Vector<EnvsElement>();
			data.put(el.name_id, chain);
		}

		chain.addElement(el);
	}

	// introduce a new nester in the frame
	public void enter(Nester el) {
		data.get(NESTER_ID).addElement(el);
	}
	
	public void enter(RemoteNester el) {
		data.get(RMT_NESTER_ID).addElement(el);
	}

	public void enterAll(StackFrame frame) {
		for(int key : frame.data.keySet()){
			Vector<EnvsElement> elems = data.get(key);
			if(elems == null){
				elems = new Vector<EnvsElement>();
				data.put(key, elems);
			}
			elems.addAll(frame.data.get(key));
		}
	}
	
	// remove a binder from the frame
	public void remove(int name_id) {
		data.remove(name_id);
	}

	public void removeAll() {
		data = new Hashtable<Integer, Vector<EnvsElement>>();
		data.put(NESTER_ID, new Vector<EnvsElement>());
		data.put(RMT_NESTER_ID, new Vector<EnvsElement>());
	}
	
	public String dump() {
		StringBuffer buf = new StringBuffer();
	
		for (Integer key : data.keySet()) {
			Vector<EnvsElement> elems = data.get(key);
			
			for (EnvsElement el : elems) {
				if (el instanceof Nester) {
					Nester n = (Nester) el;
					
					try {
						OID[] sobj = n.val.derefComplex();

						buf.append("nester " + n.val.toString() + " (");
						for (OID oid : sobj) {
							buf.append(oid.getObjectName()).append("(").append(oid.getObjectName()).append(") ");
						}
						buf.append(") ");
						
						// TODO: remote nesters
					}
					catch (Exception ex) {
						System.out.println("SBQL stack error");
						ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during a stack operation", ex);
					}
				}
				else {
					try {
						buf.append(odra.db.Database.getStore().getName(key)).append(" ");
					}
					catch (DatabaseException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		return buf.toString();
	}
	
	
	CounterData counterData;

	public final static String NESTER = Names.namesstr[Names.NESTER_ID];
	public final static String RMTNESTER = Names.namesstr[Names.RMT_NESTER_ID];
	
	public final static int NESTER_ID = Names.NESTER_ID;
	public final static int RMT_NESTER_ID = Names.RMT_NESTER_ID;
}
