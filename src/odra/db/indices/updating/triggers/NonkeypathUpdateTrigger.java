package odra.db.indices.updating.triggers;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.updating.LoggingSBQLInterpreter;
import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStoreOID;

/**
 * This class defines methods for triggers run on objects which define nonkey value 
 * in order to maintain convergence of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
public class NonkeypathUpdateTrigger extends RootUpdateTrigger {
	
	NonkeypathUpdateTrigger(OID oid) {
		super(oid);
	}
	
	public static NonkeypathUpdateTrigger initialize(OID oid, String name, OID dbidxoid, byte[] genKeyCode, byte[] cnstPool) throws DatabaseException {
		NonkeypathUpdateTrigger nonkeyPathUT = new NonkeypathUpdateTrigger(oid);
		nonkeyPathUT.initialize(UpdateTrigger.NONKEYPATH_TYPE, name, dbidxoid, genKeyCode, cnstPool);
		return nonkeyPathUT;
	}
	
	public void enableAutomaticUpdating(OID obj) throws DatabaseException {
		
		store.setIndexUpdateTrigger(obj, oid, null);		
		enableAutomaticUpdatingInSubObjects(obj, null);
		
	}
	
	public void disableAutomaticUpdating(OID obj) throws DatabaseException {
		
		store.removeIndexUpdateTrigger(obj, oid, null);		
		disableAutomaticUpdatingInSubObjects(obj, null);	
		
	}
	
	public void enableAutomaticUpdatingInSubObjects(OID obj, OID bindRoot) throws DatabaseException {

		if (obj.isAggregateObject()) {
			enableAutomaticUpdating(bindRoot);
			return;
		}
		
		super.enableAutomaticUpdatingInSubObjects(obj, bindRoot);
		
	}

	public void disableAutomaticUpdatingInSubObjects(OID obj, OID bindRoot) throws DatabaseException {

		if (obj.isAggregateObject()) {
			disableAutomaticUpdating(bindRoot);
			return;
		}
		
		super.disableAutomaticUpdatingInSubObjects(obj, bindRoot);
			
	}

	protected final OID[] getSubObjects(OID obj, OID bindRoot) throws DatabaseException {		
		
		LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule(), (DefaultStoreOID) bindRoot); 

		interpreter.setResult(new ReferenceResult(obj));
		interpreter.runCode(getGenKeyCode(), getCnstPool()); 

		return interpreter.getOidlog().toArray(new OID[0]);
	}
	


}
