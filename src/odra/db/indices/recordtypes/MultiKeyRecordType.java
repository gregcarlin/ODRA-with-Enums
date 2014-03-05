package odra.db.indices.recordtypes;

import java.util.HashSet;
import java.util.Random;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.KeyType;
import odra.db.indices.keytypes.MultiKeyType;
import odra.system.Names;

/**
 * This class makes support for indexing objects using multiple attributes.
 * This is an non-enum and non-ordered multikey record type.<br>
 * <p>Non-ordered and non-enum multikey record type has following properties:<ul>
 * <li>range queries are not supported on any key</li>
 * </ul></p> 
 * <p>Multikey record type complex object structure:<ul>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> complex object containing description of all subrecord types
 * <ul>
 * <li> first record type description</li>
 * <li> ... following recordtypes</li>
 * </ul></li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class MultiKeyRecordType extends RecordType {
	
	/**
	 * Array of subrecord types
	 */
	public RecordType[] recordType;

	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public MultiKeyRecordType(OID oid) throws DatabaseException {
		super(true);
		this.oid = oid;
		OID[] roids = oid.derefComplex();
		recordType = new RecordType[roids.length - 1];
		KeyType[] subKeyType = new KeyType[roids.length - 1];
		for (int i = 0; i < roids.length - 1; i++) {
			recordType[i] = RecordTypeKind.generateRecordType(roids[i + 1]);
			subKeyType[i] = recordType[i].keyType;
		}
		this.hashseed = getHashSeed();
		keyType = new MultiKeyType(subKeyType);
	}

	/**
	 * @param recordType array of subrecord types
	 */
	public MultiKeyRecordType(RecordType[] recordType) {
		super(true);
		this.recordType = recordType; 
		KeyType[] subKeyType = new KeyType[recordType.length];
		for (int i = 0; i < recordType.length; i++) 
			subKeyType[i] = recordType[i].keyType;
		
		keyType = new MultiKeyType(subKeyType);		
	}

	@Override
	public int keyCount() {
		return recordType.length;
	}
	
	@Override
	public void initialize(OID oid) throws DatabaseException {
		this.oid = oid;
		this.store = oid.getStore();
		hashseed = new Random().nextInt(2374);
		store.createIntegerObject(Names.HASHSEED_ID, oid, hashseed);
		for (int i = 0; i < recordType.length; i++)
			recordType[i].initialize(store.createComplexObject(Names.RECORDTYPE_ID, oid, 1));
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.MULTIKEYRECORDTYPE_ID;
	}

	/**
	 * @return indicates number of keys in this record type
	 */
	public int countRecordType() {
		return recordType.length;
	}
		
	/**
	 * @param i number of subrecord type
	 * @return subrecord type
	 */
	public RecordType getRecordType(int i) {
		return recordType[i];
	}
	
	@Override
	public boolean isOrderedRecordType() {
		for (int i = 0; i < recordType.length; i++) 
			if (!recordType[i].isOrderedRecordType())
				return false;
	
		return true;
	}
	
	@Override
	public boolean isEnumRecordType() {
		for (int i = 0; i < recordType.length; i++) 
			if (!recordType[i].isEnumRecordType())
				return false;
	
		return true;
	}
	
	@Override
	public boolean supportRangeQueries() {
		for (int i = 0; i < recordType.length; i++) 
			if (recordType[i].isEnumRecordType() || recordType[i].isOrderedRecordType())
				return true;
	
		return false;
	}
	
	/**
	 * @param i i number of subrecord type
	 * @param sinRecordType new value of subrecord type
	 */
	public void setRecordType(int i, RecordType sinRecordType) {
		recordType[i] = sinRecordType;
	}
	
	@Override
	public int hash(Object x, int rnum) {
		int hash = 0;
		for (int i = 0; i < recordType.length; i++)
			hash = hashJoin(hash, recordType[i].hash(((Object[]) x)[i], rnum), recordType[i].hashseed, rnum);
//			hash = (((hash << 5) + hash) + recordType[i].hashseed + recordType[i].hash(((Object[]) x)[i], rnum)) % rnum;
	
		return hash + rnum;
	}

	public static final int STARTHASH = 0;
	protected int hashJoin(int sumHash, int keyHash, int hashSeed, int rnum) {
		return (((sumHash << 5) + sumHash)+ hashSeed + keyHash) % rnum;
	}
	
	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {		
		
		if (isInQuery(keyValue)) {
			HashSet<Integer> sumset = new HashSet<Integer>();
			sumset.add(STARTHASH);
			for (int i = 0; i < recordType.length; i++) { 
				HashSet<Integer> keyset = recordType[i].rangeHash(((Object[]) keyValue)[i], rnum);
				HashSet<Integer> mulset = new HashSet<Integer>();
				for(Integer keyhash : keyset) 
					for(Integer sumhash : sumset)
						mulset.add(hashJoin(sumhash, keyhash, recordType[i].hashseed, rnum));
				sumset = mulset;	
			} 
			return sumset;
		}
		
		HashSet <Integer> set = new HashSet<Integer>();
		set.add(hash(keyValue, rnum)); 
		return set;
	}
	
	@Override
	public boolean isRangeQuery(Object keyValue) {
		for (int i = 0; i < recordType.length; i++) 
			if (recordType[i].isRangeQuery(((Object[]) keyValue)[i]))
				return true;
	
		return false;
	}

	@Override
	public boolean isRangeQueryOnAllRangeKeys(Object keyValue) {
		for (int i = 0; i < recordType.length; i++) 
			if (recordType[i].supportRangeQueries() && !recordType[i].isRangeQuery(((Object[]) keyValue)[i]))
				return false;
	
		return true;
	}
	
	@Override
	public boolean isInQuery(Object keyValue) {
		for (int i = 0; i < recordType.length; i++) 
			if (recordType[i].isInQuery(((Object[]) keyValue)[i]))
				return true;
	
		return false;
	}
	
	@Override
	public boolean isProperQuery(Object keyValue) {
		for (int i = 0; i < recordType.length; i++) 
			if (!recordType[i].isProperQuery(((Object[]) keyValue)[i]))
				return false;
	
		return true;
	}
	
	protected int cardinality() {
		long valCard = 1;
		for (int i = 0; i < recordType.length; i++) {
			valCard *= recordType[i].cardinality();
			if (valCard >= Integer.MAX_VALUE) valCard = 0;
		}
			
		return (int) valCard;
	}
	
	private int getHashSeed() throws DatabaseException {
		return oid.getChildAt(HASHSEED_POS).derefInt();
	}
	
	private final static int HASHSEED_POS = 0;

	@Override
	public Object getMax() throws DatabaseException {
		assert false : "getMax not applicable for non-ordered and non-enum type";
		return null;
	}

	@Override
	public Object getMin() throws DatabaseException {
		assert false : "getMin not applicable for non-ordered and non-enum type";
		return null;
	}

	@Override
	public boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException {
		return false;
	}
	
	@Override
	public Object filterKeyValue(Object keyValue) {
		return keyValue;
	}
	
}
