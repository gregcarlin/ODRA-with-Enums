package odra.db.indices.updating.triggers;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.updating.LoggingSBQLInterpreter;
import odra.store.DefaultStoreOID;
import odra.system.Names;

/**
 * This class defines methods for triggers run on objects which define nonkey value 
 * in order to maintain convergence of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
public class RootUpdateTrigger extends UpdateTrigger {

	RootUpdateTrigger(OID oid) {
		super(oid);
	}

	public static RootUpdateTrigger initialize(OID oid, String name, OID dbidxoid, byte[] genKeyCode, byte[] cnstPool) throws DatabaseException {
		RootUpdateTrigger rootUT = new RootUpdateTrigger(oid);
		rootUT.initialize(UpdateTrigger.ROOT_TYPE, name, dbidxoid, genKeyCode, cnstPool);
		return rootUT;
	}

	final void initialize(int type, String name, OID dbidxoid, byte[] genKeyCode, byte[] cnstPool) throws DatabaseException { 
		super.initialize(type, name, dbidxoid);
		store.createBinaryObject(Names.BYTECODE_ID, oid, genKeyCode, 0);
		store.createBinaryObject(Names.CNSTPOOL_ID, oid, cnstPool, 0);	
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
		
		UpdateTrigger updateTrig = UpdateTrigger.generateTrigger(this.getUpdateTriggerRef(), null);
			
		for (OID subobj: getSubObjects(obj, bindRoot)) {			
			if (subobj.isAggregateObject())
				store.setIndexUpdateTrigger(subobj, updateTrig.oid, null);
			else
				updateTrig.enableAutomaticUpdating(subobj);
		}	
		
	}

	public void disableAutomaticUpdatingInSubObjects(OID obj, OID bindRoot) throws DatabaseException {

		UpdateTrigger updateTrig = UpdateTrigger.generateTrigger(this.getUpdateTriggerRef(), null);
			
		for (OID subobj: getSubObjects(obj, bindRoot)) {			
			if (subobj.isAggregateObject()) 
				store.removeIndexUpdateTrigger(subobj, updateTrig.oid, null);
			else
				updateTrig.disableAutomaticUpdating(subobj);
		}
			
	}
	
	protected OID[] getSubObjects(OID dataEntry, OID bindRoot) throws DatabaseException {		
		
		LoggingSBQLInterpreter interpreter = LoggingSBQLInterpreter.getLoggingSBQLInterpreterInstance(getModule(), (DefaultStoreOID) bindRoot); 
		
		interpreter.runCode(getGenKeyCode(), getCnstPool()); 

		return interpreter.getOidlog().toArray(new OID[0]);
	}
	
	
	byte[] getGenKeyCode() throws DatabaseException {
		return getGenKeyCodeRef().derefBinary();
	}

	byte[] getCnstPool() throws DatabaseException {
		return getCnstPoolRef().derefBinary();
	}	
	
	/***********************************
	 * access to general subobjects describing the RootObjectUpdateInformation
	 * */ 
	
	private final OID getGenKeyCodeRef() throws DatabaseException {
		return oid.getChildAt(N2KBYTECODE_POS);
	}	

	private final OID getCnstPoolRef() throws DatabaseException {
		return oid.getChildAt(N2KCNSTPOOL_POS);
	}	
	
	private final static int N2KBYTECODE_POS = 4;
	private final static int N2KCNSTPOOL_POS = 5;
	
}
