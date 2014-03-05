package odra.db.indices.recordtypes;

import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.keytypes.KeyType;
import odra.db.indices.keytypes.KeyTypeKind;
import odra.system.Names;

/**
 * This class is a super-class for description of all types of records stored 
 * in indexes. 
 * The goal is to make data indexing technique independent of indexed data
 * <br>
 * Record type can be serialized in databases store in an complex object and first child 
 * indicates the particular type of data access according to RecordTypeKind enumeration.  
 * <br><br>
 * Record type complex object structure:<ul>
 * <li> ID of record type (according to RecordTypeKind "enumeration")</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> ... (particular record type description) </li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
public abstract class RecordType {

	/**
	 * Key type associated with index current index record
	 */
	public KeyType keyType;
	
	protected OID oid;
	protected IDataStore store;
	protected int hashseed;
	protected boolean obligatory;
	
	/**
	 * @param obligatory true if current key (record) requires to have value specified in index call
	 */
	protected RecordType(boolean obligatory){
		this.obligatory = obligatory;
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	protected RecordType(OID oid) throws DatabaseException{
		this.oid = oid;
		this.store = oid.getStore();
		this.hashseed = getHashSeed();
		this.keyType = KeyTypeKind.generateKeyType(getKeyTypeID());
		this.obligatory = getObligatory();
	}
	
	/**
	 * @param dataAccess sets data access type associated with current index
	 */
	public final void setDataAccess(DataAccess dataAccess) {
		keyType.setDataAccess(dataAccess);
	}
	
	/**
	 * Initializes the record type in the database by creating some system-level subobjects. 
	 * @param oid empty complex object oid
	 * @throws DatabaseException
	 */
	public void initialize(OID oid) throws DatabaseException {
		this.oid = oid;
		this.store = oid.getStore();
		store.createIntegerObject(Names.RECORDTYPEID_ID, oid, getRecordTypeID());
		store.createIntegerObject(Names.KEYTYPEID_ID, oid, keyType.getKeyTypeID());
		store.createBooleanObject(Names.OBLIGATORYID_ID, oid, obligatory);
		if (hashseed == 0) hashseed = new Random().nextInt(2374);
		store.createIntegerObject(Names.HASHSEED_ID, oid, hashseed);
	}
	
	/**
	 * @return indicates number of keys in this record type (for non-multikey record types the number is 1)
	 */
	public int keyCount() {
		return 1;
	}
	
	/**
	 * @return oid complex object containing recordtype description
	 */
	public final OID getOID() {
		return oid;
	}
	
	/**
	 * @return the smallest key value that can occur
	 * @throws DatabaseException
	 */
	public abstract Object getMin() throws DatabaseException;
	
	/**
	 * @return the bigyest key value that can occur
	 * @throws DatabaseException
	 */
	public abstract Object getMax() throws DatabaseException;
	
	
	/**
	 * @param keyValue value argument
	 * @param rnum twice a current number of buckets to be split
	 * @return hash code calculated on given key value 
	 */
	public abstract int hash(Object keyValue, int rnum);
	
	/**
	 * @param keyValue value, range of values or set of values 
	 * @param rnum twice a current number of buckets to be split
	 * @return set of all possible hash codes calculated on given key values 
	 */
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {		
		
		if (isInQuery(keyValue)) {
			Object[] keys = ((Object[])((Object[])keyValue)[1]);
			HashSet<Integer> set = new HashSet<Integer>(keys.length);				
			for(int i = 0; i < keys.length; i++) 
				set.add(hash(keys[i], rnum));
			return set;
		}
		
		HashSet <Integer> set = new HashSet<Integer>();
		set.add(hash(keyValue, rnum)); 
		return set;
	}
	
	/**
	 * @param keyValue value, range of values or set of values 
	 * @param rnum twice a current number of buckets to be split
	 * @return set of all possible hash codes calculated on given key values 
	 * that indicate buckets which may contain items searched (filtering necessary) 
	 */
	public HashSet<Integer> limitHash(Object keyValue, int rnum) {
		return rangeHash(keyValue, rnum);
	}
	
	
	/**
	 * @param keyValue value, range of values or set of values
	 * @return true if key value object contains range query
	 */
	public boolean isRangeQuery(Object keyValue) {
		return (keyValue instanceof Object[]) && ((Integer) ((Object[]) keyValue)[0] == KeyType.RANGE_KEY);
	}
	
	/**
	 * If in index call there are range queries on all range keys then not all buckets returned by rangeHash must be filtered.<br>
	 * Note: there is one exception for enum record type
	 * @param keyValue value, range of values or set of values
	 * @return true if key value object contains range query on all of keys which support range queries
	 */
	public boolean isRangeQueryOnAllRangeKeys(Object keyValue) {
		return isRangeQuery(keyValue) && supportRangeQueries();
	}
	
	/**
	 * @param keyValue value, range of values or set of values
	 * @return true if key value object contains query for a set of values on one of the keys
	 */
	public boolean isInQuery(Object keyValue) {
		return (keyValue instanceof Object[]) && ((Integer) ((Object[]) keyValue)[0] == KeyType.IN_KEY);
	}
	
	/**
	 * @param keyValue value, range of values or set of values
	 * @return true if key value represents a proper query on current key (record) 
	 */
	public boolean isProperQuery(Object keyValue) {
		if (keyValue instanceof Object[]) {
			if (((Object[]) keyValue).length != 2)
				return false;				
			if (!(((Object[]) keyValue)[0] instanceof Integer)
				|| !(((Object[]) keyValue)[1] instanceof Object[]));
			if ((Integer) ((Object[]) keyValue)[0] == KeyType.RANGE_KEY) {
				if (((Object[]) ((Object[]) keyValue)[1]).length != 4)
					return false;
				return supportRangeQueries();
			}
			return ((Integer) ((Object[]) keyValue)[0] == KeyType.IN_KEY);
		}
		return true;
	}
	
	/**
	 * @return id of type according to RecordTypeKind class
	 */
	public abstract int getRecordTypeID();
	
	/**
	 * @return whether order of the key values influences location of the data in the index 
	 */
	public abstract boolean isOrderedRecordType();
	
	/**
	 * @return whether record type is enum - i.e. all possible key values are known 
	 * to record type and their cardinality is limited
	 */
	public abstract boolean isEnumRecordType();
	
	/**
	 * Performs adjustments for enum recordtypes (only if necessary) in order to accept new key value. 
	 * After adjustments index has to be rebuilt. 
	 * @param newKeyValue simple single keyvalue (or single combination)
	 * @return true if recordtype was adjusted to given key value  
	 * @throws DatabaseException 
	 */
	public abstract boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException;
	
	/**
	 * Helper for enum recordtypes - filters unnecessary elements from key value.   
	 * @param keyValue keyValue
	 * @return filtered representation of the key value  
	 * @throws DatabaseException 
	 */
	public Object filterKeyValue(Object keyValue) {
	
		Object[] keys = ((Object[])((Object[])keyValue)[1]);
		Vector<Object> filteredKeys = new Vector<Object>(keys.length);				
		for(Object key: keys)
			if (!(filterKeyValue(key) instanceof Object[])) // objects array means filtered key (empty in) 
				filteredKeys.add(key);

		if (filteredKeys.size() == 1)
			return filteredKeys.firstElement();			
					
		filteredKeys.copyInto((Object[])(((Object[])keyValue)[1] = new Object[filteredKeys.size()]));
		
		return keyValue;
	}

	
	/**
	 * @return whether current key (record) requires to have value specified in index call
	 */
	public boolean isObligatory() {
		return obligatory; 
	}
	
	/**
	 * @return whether current key (record) supports range queries in index call
	 */
	public boolean supportRangeQueries() {
		return (isOrderedRecordType() || isEnumRecordType());
	}
	
	// NOTE: cardinality 0 means infinity :)
	protected abstract int cardinality();
	
	/**
	 * @return cardinality of key values
	 */
	public int valuesCardinality() {
		int valCard = cardinality();
		return valCard == 0 ? Integer.MAX_VALUE : valCard;
	}
	
	/**
	 * @return hashseed used to modify hash function result
	 */
	public final int getHashSeedVal() {
		return hashseed;
	}
	
	private int getHashSeed() throws DatabaseException {
		return oid.getChildAt(HASHSEED_POS).derefInt();
	}

	private int getKeyTypeID() throws DatabaseException {
		return  getKeyTypeIDRef().derefInt();
	}
	
	private boolean getObligatory() throws DatabaseException {
		return oid.getChildAt(OBLIGATORYID_POS).derefBoolean();
	}
	
	private OID getKeyTypeIDRef() throws DatabaseException {
		return oid.getChildAt(KEYTYPEID_POS);
	}
	
	private final static int RECORDTYPEID_POS = 0;
	private final static int KEYTYPEID_POS = RECORDTYPEID_POS + 1;
	private final static int OBLIGATORYID_POS = KEYTYPEID_POS + 1;
	protected final static int HASHSEED_POS = OBLIGATORYID_POS + 1;

}
