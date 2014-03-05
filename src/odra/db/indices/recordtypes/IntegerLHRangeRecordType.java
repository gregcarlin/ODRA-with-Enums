package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.IntegerKeyType;
import odra.db.indices.keytypes.KeyType;
import odra.system.Names;

/**
 * This is a enum and ordered record type.<br>
 * <p>Ordered record type has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>key have limited number of distinct values (valCard)</li> 
 * <li>in case if number LH buckets is smaller then key values cardinality (rnum < valCard) then hash can take all possible values 0 .. rnum-1</li>
 * <li>when LH buckets number gets larger then key values cardinality (valCard <= rnum) then hash takes valCard distinct values and 2 different values have different hash value. </li>

 * <li>FIXME: may not be obligatory because hash function span all possible values if cardinality is greater then key (obligatory || lhrnum < valCard)</li>
 * </ul></p>  
 * NOTE: Cannot be used in MultiEnumKeyRecordType. 
 * 
 * <p>Integer LH range record type complex object structure:<ul>
 * <li> ID of record type (INTEGERLHRANGERECORDTYPE_ID)</li>
 * <li> ID of key type (INTEGERKEYTYPE_ID)</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>min</b> integer to specify range of partitioning in index</li>
 * <li> <b>max</b> integer to specify range of partitioning in index</li>
 * <li> <b>lhrnum</b> if number of buckets is above lhrnum records are not moved during split or merge</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class IntegerLHRangeRecordType extends LHRangeRecordType {
	
	// if rnum is above lhrnum records are not moved during split or merge
	private int lhrnum; 
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public IntegerLHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
		lhrnum = getLHrnum();
	}
	
	/**
	 * @param min integer to specify range of partitioning in index
	 * @param max integer to specify range of partitioning in index
	 * @param bucketsCount initial number of buckets in index
	 */
	public IntegerLHRangeRecordType(int min, int max, int bucketsCount) {
		this.min = min;
		this.max = max;
		while (bucketsCount < max - min + 1) bucketsCount <<= 1;
		while ((bucketsCount >> 1) >= max - min + 1) bucketsCount >>= 1;
		this.lhrnum = bucketsCount;
		keyType = new IntegerKeyType();
	}

	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		store.createIntegerObject(Names.LHRNUM_ID, oid, lhrnum);
	}	
	
	@Override
	protected int hash(int range, int bitnum, int rnum) {
		while (rnum > lhrnum) {
			bitnum--;
			rnum >>= 1;
		}
		return super.hash(range, bitnum, rnum);
	}
	
	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
		if (rnum >= valuesCardinality())
			return new HashSet<Integer>();
		return super.limitHash(x, rnum);
	}
	
	@Override
	protected final int rangeSelect(Object value, int rnum) {
		if (isBelowMin(value)) return 0;
		else if (isEqualAboveMax(value)) {
			if (rnum >= lhrnum) return ((Integer) max - (Integer) min);
			return (int) ((((long) ((Integer) max - (Integer) min)) * rnum) / lhrnum);		
		}
		if (rnum >= lhrnum) return ((Integer) value - (Integer) min);
		return (int) ((((long) ((Integer) value - (Integer) min)) * rnum) / lhrnum);
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.INTEGERLHRANGERECORDTYPE_ID;
	}
	
	@Override
	public boolean isEnumRecordType() {
		return true;
	}

	@Override
	protected int cardinality() {
		return ((Integer) max - (Integer) min + 1);
	}
	
	private void setMin(Object newMin) throws DatabaseException {
		getMinRef().updateIntegerObject((Integer) newMin);
		min = newMin;
		adjustLHrnum();
	}

	private void setMax(Object newMax) throws DatabaseException {
		getMaxRef().updateIntegerObject((Integer) newMax);
		max = newMax;
		adjustLHrnum();
	}
	
	private void adjustLHrnum() throws DatabaseException {
		while (this.lhrnum < (Integer) max - (Integer) min + 1) this.lhrnum <<= 1;
		while ((this.lhrnum >> 1) >= (Integer) max - (Integer) min + 1) this.lhrnum >>= 1;
		getLHrnumRef().updateIntegerObject(this.lhrnum);
	}
	
	private int getLHrnum() throws DatabaseException {
		return getLHrnumRef().derefInt();
	}
	
	private OID getLHrnumRef() throws DatabaseException {
		return oid.getChildAt(LHRNUM_POS);
	}
	
	private final static int LHRNUM_POS = MAX_POS + 1;

	@Override
	public boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException {
		// TODO: TK sprawdzić wpływ MIN I MAX na organizacje indeksu!!!
		
		if (keyType.isLess(newKeyValue, min)) {
			setMin(newKeyValue);
			return true;
		}
		if (keyType.isLess(max, newKeyValue)) {
			setMax(newKeyValue);
			return true;
		}	
		return false;
	}
	
	@Override
	public Object filterKeyValue(Object keyValue) {
		if (isRangeQuery(keyValue))
			return keyValue;
		
		if (isInQuery(keyValue))
			return super.filterKeyValue(keyValue);
					
		if (!(keyType.isLess(keyValue, min) || keyType.isLess(max, keyValue)))
			return keyValue;
		else
			return new Object[] { KeyType.IN_KEY, new Object[0] }; 	
	}
		
}
