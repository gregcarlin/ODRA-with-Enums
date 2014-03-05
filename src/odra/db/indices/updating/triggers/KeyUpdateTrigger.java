package odra.db.indices.updating.triggers;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * This class defines methods for triggers run on objects which define index key value 
 * in order to maintain convergence of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
public class KeyUpdateTrigger extends NonkeyUpdateTrigger {
	
	KeyUpdateTrigger(OID oid, OID nonkeyoid) throws DatabaseException {
		super(oid, nonkeyoid);
	}
	
	KeyUpdateTrigger(OID oid) {
		super(oid);
	}
	
	public static KeyUpdateTrigger initialize(OID oid, String name, OID dbidxoid) throws DatabaseException {
		KeyUpdateTrigger keyUT = new KeyUpdateTrigger(oid);
		keyUT.initialize(UpdateTrigger.KEYVALUE_TYPE, name, dbidxoid);
		return keyUT;
	}
	
	protected KeyUpdateTrigger getKeyUpdateTrigger() throws DatabaseException {
		return this;
	}
	
	public void enableAutomaticUpdating(OID obj) throws DatabaseException {
		assert false : "unused"; 
		store.setIndexUpdateTrigger(obj, oid, nonkey.value);				
	}
	
	public void disableAutomaticUpdating(OID obj) throws DatabaseException {
		store.removeIndexUpdateTrigger(obj, oid, nonkey.value);
		beforeoids.remove(obj);
	}
	
}
