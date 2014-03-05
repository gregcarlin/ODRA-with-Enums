package odra.db.indices.dataaccess;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.system.Names;
import odra.system.Sizes;

/**
 * 
 * This class provides indexing for database objects using any functions combination.
 * Single index record contain following<br>
 * | Object OID |<br><br>
 *
 * This is the most general data access and can be used for all kinds of 
 * database indices.
 * <br><br>
 * DBObjectToKeyAccess complex object structure:<ul>
 * <li> DBOBJ2KEYACCESS_ID </li>
 * <li> serialized bytecode generating key values for objects</li>
 * <li> constant pool for bytecode</li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
public class DBObjectToKeyAccess extends DataAccess {

	private SBQLInterpreter interpreter;

	// Serialized Juliet bytecode calculating objects key value attributes
	byte[] genKeyCode;
	byte[] cnstPool;	
	
	/**
	 * Creates DBObjectToKeyAccess 
	 * serialized in complex object given by oid parameter.
	 * @param oid complex object containing dataaccess description
	 * @param module index call execution context module
	 * @throws DatabaseException 
	 */
	public DBObjectToKeyAccess(OID oid, DBModule module) throws DatabaseException {
		super(oid);
		
		interpreter = new SBQLInterpreter(module);
		this.genKeyCode = getGenKeyCode();
		this.cnstPool = getCnstPool();
	}
	
	/**
	 * Creates uninitialized DBObjectToKeyAccess.
	 * @param genKeyCode serialized bytecode generating key values for objects
	 * @param cnstPool constant pool for bytecode
	 * @throws DatabaseException
	 */
	public DBObjectToKeyAccess(byte[] genKeyCode, byte[] cnstPool) throws DatabaseException {
		super();
		this.genKeyCode = genKeyCode;
		this.cnstPool = cnstPool;		
	}

	public DBObjectToKeyAccess() {
		super();
		this.genKeyCode = new byte[0];
		this.cnstPool = new byte[0];
	}

	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		store.createBinaryObject(Names.BYTECODE_ID, oid, genKeyCode, 0);
		store.createBinaryObject(Names.CNSTPOOL_ID, oid, cnstPool, 0);
	}
	
	/** 
	 * Obtains keys for given nonkey oid 
	 * @param nonkey oid 
	 * @return returns keys for given index record if possible 
	 * @throws DatabaseException
	 */
	public final Object nonkey2key(Object nonkey) throws DatabaseException { 
		
		interpreter.setResult((ReferenceResult) nonkey);		
		interpreter.runCode(genKeyCode, cnstPool);
		
		return interpreter.getResult();
	}
	
	/** 
	 * Obtains keys for given nonkey oid. Used in automatic index updating
	 * @param nonkey oid
	 * @param interpreter external instance of the interpreter  
	 * @return returns keys for given index record if possible 
	 * @throws DatabaseException
	 */
	public final Object nonkey2key(Object nonkey, SBQLInterpreter interpreter) throws DatabaseException { 
		
		interpreter.setResult((ReferenceResult) nonkey);		
		interpreter.runCode(genKeyCode, cnstPool);
		
		return interpreter.getResult();
	}
	
	/** 
	 * Obtains key values for given nonkey oid 
	 * @param nonkey oid 
	 * @return returns key value for given index record if possible 
	 * @throws DatabaseException
	 */
	private final Object nonkey2keyValue(ReferenceResult nonkey) throws DatabaseException { 
		
		return recordType.keyType.key2KeyValue(nonkey2key(nonkey));
	}
	
	@Override
	public Object key2keyValue(Object key) throws DatabaseException {
		return recordType.keyType.key2KeyValue(key); 
	}	
	
	@Override
	public Object record2nonkey(byte[] record) {
		return new ReferenceResult(new DefaultStoreOID(ByteBuffer.wrap(record).getInt(), (DefaultStore) store));
	}

	@Override
	public byte[] prepareRecordArray(Object key, Object nonkey) throws DatabaseException {
		return ByteBuffer.allocate(getNonkeyRecordSize()).putInt(((DefaultStoreOID) ((ReferenceResult) nonkey).value).getOffset()).array();
	}

	@Override
	public Object record2keyValue(byte[] record) throws DatabaseException { 		
		return nonkey2keyValue(((ReferenceResult) record2nonkey(record)));
	}			
	
	@Override
	protected int getKindID() {
		return DataAccessKind.DBOBJ2KEYACCESS_ID;
	}
	
	/**
	 * @return 4 - in case of DBObjectToKeyAccess individual record stored in index consist of one int value 
	 */
	public int getNonkeyRecordSize() {
		return Sizes.INTVAL_LEN;
	}

	/**
	 * @return null - default value returned by index if nonkey has not been found
	 */
	public Object getNotFoundValue() {
		return null;
	}	
	
	private byte[] getGenKeyCode() throws DatabaseException {
		return getGenKeyCodeRef().derefBinary();
	}

	private byte[] getCnstPool() throws DatabaseException {
		return getCnstPoolRef().derefBinary();
	}	
	
	private final OID getGenKeyCodeRef() throws DatabaseException {
		return oid.getChildAt(N2KBYTECODE_POS);
	}	

	private final OID getCnstPoolRef() throws DatabaseException {
		return oid.getChildAt(N2KCNSTPOOL_POS);
	}	
	
	private final static int N2KBYTECODE_POS = 1;
	private final static int N2KCNSTPOOL_POS = 2;

}
