package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class represents the virtual objects procedure
 * stored outside the view, in the main block of the module
 * (as a special purpose global procedure).
 * 
 * @author raist
 */

public class DBVirtualObjectsProcedure extends DBProcedure {
	/**
	 * Initializes a new DBVirtualObjectsProcedure object using a reference
	 * to an existing procedure object (or an empty complex object).
	 * @param oid 
	 */
	public DBVirtualObjectsProcedure(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();		
	}	
	
	/**
	 * Initializes an empty complex object.
	 * @param objBody intermediate code of the body (unused)
	 * @param binBody binary code of the body
	 * @param constants list of constants used by this procedure
	 * @param viewref references to a DBView object to which this procedure belongs
	 */
	public void initialize( byte[] objBody, byte[] binBody, byte[] constants, byte[] catches, OID viewref) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT);
		store.createBinaryObject(store.addName(Names.namesstr[Names.DEBUG_ID]), oid, objBody, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.BYTECODE_ID]), oid, binBody, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.CNSTPOOL_ID]), oid, constants, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.CATCH_ID]), oid, catches, 0);
		store.createPointerObject(store.addName(Names.namesstr[Names.VIEW_REF_ID]), oid, viewref); // this points at the view containg this virtual objects procedure
	}

	public void initialize(byte[] astBody, byte[] objBody, byte[] binBody, byte[] constants) throws DatabaseException {
		assert false : "this method is not applicable";
	}
	
	/**
	 * @return true if object's oid represents a valid virtual objects procedure
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.VIRTUAL_OBJECTS_PROCEDURE_OBJECT;
	}

	/** 
	 * @return returns the view to which to object belongs
	 */
	public OID getView() throws DatabaseException {
		return getViewRef().derefReference();
	}
	
	private final OID getViewRef() throws DatabaseException {
		return oid.getChildAt(VIEW_POS);
	}

	private final static int VIEW_POS = 5;
	
	public final static int FIELD_COUNT = 6;	
}
