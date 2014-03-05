package odra.db.indices.dataaccess;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.recordtypes.RecordType;
import odra.system.Names;

/**
 * This class is a super-class for 
 * providing different kinds of access to an indexed data. 
 * The goal is to make data indexing technique indepentent of indexed data.
 * <br>
 * Data access can be serialized in databases store in an complex object and first child 
 * indicates the particular type of data access according to DataAccessKind enumeration.  
 * <br><br>
 * Data access complex object structure:<ul>
 * <li> ID of data access </li>
 * <li> ... (particular data access description) </li>
 * </ul>
 * 
 * @author tkowals	
 * @version 1.0
 */
public abstract class DataAccess {

	protected RecordType recordType;
	protected OID oid;
	protected IDataStore store;
	
	/**
	 * @param oid complex object containing dataaccess description
	 */
	protected DataAccess(OID oid) {
		this.oid = oid;
		this.store = oid.getStore();
	}
	
	protected DataAccess() {
		oid = null;
		store = null;
	}
	
	/**
	 * Sets record type according to recordtypes definitions.
	 * Used only during generating new index.
	 * @param recordType description of key records for current index
	 */
	public final void setRecordType(RecordType recordType) {
		this.recordType = recordType;
	}
	
	/**
	 * Initializes the data access in the database by creating some system-level subobjects. 
	 * @param oid empty complex object oid
	 * @throws DatabaseException
	 */
	public void initialize(OID oid) throws DatabaseException {
		this.oid = oid;
		store = oid.getStore();
		store.createIntegerObject(Names.DATAACCESSID_ID, oid, getKindID());
	}
	
	
	/**
	 * Converts individual record read from index to appropriate nonkey value.
	 * @param record value array retrieved from index 
	 * @return nonkey value converted from byte array
	 */
	public abstract Object record2nonkey(byte[] record);
	
	/**
	 * Creates individual index record for given key and nonkey.  
	 * @param key key
	 * @param nonKey nonkey
	 * @return index record as array
	 * @throws DatabaseException  
	 */
	public abstract byte[] prepareRecordArray(Object key, Object nonkey) throws DatabaseException;
	
	/** 
	 * Obtains keys for given nonkey 
	 * @param nonkey oid 
	 * @return returns keys for given index record if possible 
	 * @throws DatabaseException
	 */
	public abstract Object nonkey2key(Object nonkey) throws DatabaseException;
	
	/**
	 * Evaluates the key to obtain actual value used directly in indexing 
	 * @param key
	 * @return value of given key
	 * @throws DatabaseException
	 */
	public abstract Object key2keyValue(Object key) throws DatabaseException;
	
	/**
	 * Obtains key values which were used to store given record in the index 
	 * @param record array value retrieved from index 
	 * @return returns key value for given index record if possible
	 * @throws DatabaseException 
	 */	
	public abstract Object record2keyValue(byte[] record) throws DatabaseException;
	
	/**
	 * @return id of data access according to DataAccessKind classification
	 */
	protected abstract int getKindID();
	
	/**
	 * @return size of individual records stored in index
	 */
	public abstract int getNonkeyRecordSize();
	
	/**
	 * @return default value returned by index if nonkey has not been found
	 */
	public abstract Object getNotFoundValue();

	public final IDataStore getStore() {
		return store;
	}
	
}
