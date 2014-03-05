package odra.db.indices.updating.triggers;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.updating.IndexableStore;
import odra.db.objects.data.DBIndex;
import odra.db.objects.data.DBModule;

/**
 * This class defines common methods for triggers maintaining convergence of data and indices.
 * 
 * @author tkowals
 * @version 1.0
 */
abstract public class UpdateTrigger {

	OID oid;
	IndexableStore store;
	
	protected UpdateTrigger(OID oid) {
		this.oid = oid;
		store = (IndexableStore) Database.getStore();
	}
		
	public static final UpdateTrigger generateTrigger(OID oid, OID nonkeyoid) throws DatabaseException {
		switch (oid.getChildAt(UPDATETRIGTYPE_POS).derefInt()) {
		case NONKEYPATH_TYPE:
			return new NonkeypathUpdateTrigger(oid);
		case ROOT_TYPE:
			return new RootUpdateTrigger(oid);
		case NONKEY_TYPE:
			return new NonkeyUpdateTrigger(oid, nonkeyoid);
		case KEYVALUE_TYPE:
			return new KeyUpdateTrigger(oid, nonkeyoid);
		}		
		assert false : "unknown record type";
		return null;
	}
	
	final void initialize(int type, String name, OID dbidxoid) throws DatabaseException { 
		store.createIntegerObject(store.addName("$type"), oid, type);
		store.createIntegerObject(store.addName("$name"), oid, store.getNameId(name));
		store.createReferenceObject(store.addName("$dbidxoid"), oid, dbidxoid);
		store.createComplexObject(store.addName("$subupdtrig"), oid, 1);
	}
	
	abstract public void enableAutomaticUpdating(OID obj) throws DatabaseException;
	
	abstract public void disableAutomaticUpdating(OID obj) throws DatabaseException;
	
	protected final DBModule getModule() throws DatabaseException {
		return new DBModule(getDBIndexOID().getParent().getParent());
	}
	
	protected final Index getIndex() throws DatabaseException {
		return new DBIndex(getDBIndexOID()).getIndex();
	}
	
	public final int getUpdateType() throws DatabaseException {
		return getUpdateTypeRef().derefInt();
	}
	
	final int getObjectsNameId() throws DatabaseException {
		return getObjectsNameIdRef().derefInt();
	}
	
	final OID getDBIndexOID() throws DatabaseException {
		return getDBIndexOIDRef().derefReference();
	}
		
	/***********************************
	 * access to general subobjects describing the ObjectUpdateInformation
	 * */ 
	
	public final OID getUpdateTriggerRef() throws DatabaseException {
		return oid.getChildAt(SUBUPDATETRIGGER_POS);
	}	
	
	private final OID getUpdateTypeRef() throws DatabaseException {
		return oid.getChildAt(UPDATETRIGTYPE_POS);
	}
	
	private final OID getObjectsNameIdRef() throws DatabaseException {
		return oid.getChildAt(OBJECTSNAMEID_POS);
	}
	
	private final OID getDBIndexOIDRef() throws DatabaseException {
		return oid.getChildAt(DBIDXOID_POS);
	}
	
	private static final int UPDATETRIGTYPE_POS = 0;
	private static final int OBJECTSNAMEID_POS = 1;
	private static final int DBIDXOID_POS = 2;
	private static final int SUBUPDATETRIGGER_POS = 3; 
	
//	private static final int FIELDSCOUNT = 43;
	
	// update types
	public static final int ROOT_TYPE = 0;
	public static final int NONKEYPATH_TYPE = 1;
	public static final int NONKEY_TYPE = 2;
	public static final int KEYVALUE_TYPE = 3;
		
}
