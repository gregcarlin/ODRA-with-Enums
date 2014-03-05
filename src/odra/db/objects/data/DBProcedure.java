package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides functionality of procedure objects stored
 * in the database.
 * 
 * @author raist
 */

public class DBProcedure extends DBObject {
	/**
	 * Initializes a new DBProcedure object using a reference
	 * to an existing procedure object (or an empty complex object).
	 * @param oid 
	 */
	public DBProcedure(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();		
	}

	/**
	 * Initializes an empty complex object.
	 * @param debugBody debug code of the body 
	 * @param binBody binary code of the body
	 * @param constants list of constants used by this procedure
	 * @param catches description of catch blocks in the procedure
	 */
	public void initialize( byte[] debugBody, byte[] binBody, byte[] constants, byte[] catches) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.PROCEDURE_OBJECT);
		store.createBinaryObject(store.addName(Names.namesstr[Names.DEBUG_ID]), oid, debugBody, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.BYTECODE_ID]), oid, binBody, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.CNSTPOOL_ID]), oid, constants, 0);
		store.createBinaryObject(store.addName(Names.namesstr[Names.CATCH_ID]), oid, catches, 0);
	}

	/**
	 * Returns the OID of the procedure
	 */
	public OID getOID() {
		return oid;
	}

	
	/**
	 * @return true if object's oid represents a valid procedure
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.PROCEDURE_OBJECT;
	}
	

	
	/**
	 * @return debug code of the procedure
	 */
	public byte[] getDebugCode() throws DatabaseException {
		return getDebugRef().derefBinary();
	}
	
	/**
	 * @param val new debug code of the procedure
	 */
	public void setDebugCode(byte[] val) throws DatabaseException {
		getDebugRef().updateBinaryObject(val);
	}
	
	/**
	 * @return binary code of the procedure
	 */
	public byte[] getBinaryCode() throws DatabaseException {
		return getBinaryRef().derefBinary();
	}

	/**
	 * @param val new binary code of the procedure
	 */
	public void setBinaryCode(byte[] val) throws DatabaseException {
		getBinaryRef().updateBinaryObject(val);
	}

	/** 
	 * @return constant pool of the procedure
	 */
	public byte[] getConstantPool() throws DatabaseException {
		return getConstantsRef().derefBinary();
	}

	/**
	 * @param data new constant pool of the procedure
	 */
	public void setConstantPool(byte[] data) throws DatabaseException {
		getConstantsRef().updateBinaryObject(data);
	}
	/** 
	 * @return description of catch blocks for the procedure
	 */
	public byte[] getExceptionTable() throws DatabaseException {
		return getExceptionTableRef().derefBinary();
	}

	/**
	 * @param data - description of catch blocks for the procedure
	 */
	public void setExceptionTable(byte[] data) throws DatabaseException {
		getExceptionTableRef().updateBinaryObject(data);
	}
	/***********************************
	 * access to subobjects describing the procedure
	 * */
	
	
	
	private final OID getDebugRef() throws DatabaseException {
		return oid.getChildAt(DEBUG_POS);
	}

	private final OID getBinaryRef() throws DatabaseException {
		return oid.getChildAt(BIN_POS);
	}
	
	private final OID getConstantsRef() throws DatabaseException {
		return oid.getChildAt(CONSTANTS_POS);
	}
	
	private final OID getExceptionTableRef() throws DatabaseException {
		return oid.getChildAt(EXCEPTION_TABLE_POS);
	}
	
	private final static int DEBUG_POS = 1;
	private final static int BIN_POS = 2;
	private final static int CONSTANTS_POS = 3;
	private final static int EXCEPTION_TABLE_POS = 4;
	
	
	public final static int FIELD_COUNT = 5;
}
