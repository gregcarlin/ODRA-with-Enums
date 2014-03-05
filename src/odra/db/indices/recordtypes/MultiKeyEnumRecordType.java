package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;

/**
 * This class makes support for indexing objects using multiple attributes.
 * This is an enum multikey record type.<br>
 * <p>Enum multikey record type has following properties:<ul>
 * <li>keys have limited number of distinct values combination (valCard)</li>
 * <li>range queries can be supported on any key</li>
 * <li>when LH buckets number gets larger then key values cardinality (valCard <= rnum) 
 * then hash takes valCard distinct values and 2 different values have different hash value
 * and no additional filtering is necessary when searching index.</li>
 * </ul></p>
 * <p>Multikey enum record type complex object structure:<ul>
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
public class MultiKeyEnumRecordType extends MultiKeyLHRangeRecordType {
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public MultiKeyEnumRecordType(OID oid) throws DatabaseException {
		super(oid);
	}
	
	/**
	 * @param recordType recordType array of subrecord types
	 */
	public MultiKeyEnumRecordType(RecordType[] recordType) {
		super(recordType);
		
		int hashseed = 1;
		for(RecordType subRecordType: recordType) {
			subRecordType.hashseed = hashseed;
			hashseed *= subRecordType.cardinality();
		}
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.MULTIKEYLHENUMRECORDTYPE_ID;
	}

	@Override
	public int hash(Object x, int rnum) {
		int hash = 0;
		for (int i = 0; i < recordType.length; i++)
			hash = hashJoin(hash, recordType[i].hash(((Object[]) x)[i], rnum), recordType[i].hashseed, rnum);

		return hash + rnum;
	}
	
	protected static final int STARTHASH = 0;
	@Override
	public int hashJoin(int sumHash, int keyHash, int hashSeed, int rnum) {
		return (int) (sumHash + (long) hashSeed * keyHash) % rnum;
	}
	
	@Override
	public boolean isOrderedRecordType() {
		return false;
	}
	
	@Override
	public boolean isEnumRecordType() {
		return true;
	}

	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
		if (rnum >= cardinality()) 
			return new HashSet<Integer>();
		return super.limitHash(x, rnum);
	}

}
