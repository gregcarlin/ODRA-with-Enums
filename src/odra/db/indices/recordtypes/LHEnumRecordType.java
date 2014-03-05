package odra.db.indices.recordtypes;

import java.util.HashSet;
import java.util.TreeSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.KeyType;
import odra.system.Names;

/**
 * This is a enum and ordered record type.<br>
 * <p>Enum record type has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>key have limited number of distinct values (valCard)</li> 
 * <li>in case if number LH buckets is smaller then key values cardinality (rnum < valCard) then hash can take all possible values 0 .. rnum-1</li>
 * <li>when LH buckets number gets larger then key values cardinality (valCard <= rnum) then hash takes valCard distinct values and 2 different values have different hash value. </li>

 * <li>FIXME: may not be obligatory because hash function span all possible values if cardinality is greated then key (obligatory || lhrnum < valCard)</li>
 * </ul></p>
 * <p>LH enum range record type complex object structure:<ul>
 * <li> ID of record type (LHENUMRECORDTYPE_ID)</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>keyvalues</b> complex object containing all possible keyvalues ordered
 * <ul>
 * <li> minimal indexed key value</li>
 * <li> ... ordered key values
 * <li> maximum indexed key value</li>
 * </ul></li>
 * <li> <b>lhrnum</b> if number of buckets is above lhrnum records are not moved during split or merge</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class LHEnumRecordType extends LHOrderedRecordType {

	int valCard;
	
	TreeSet tempSet;

	// if rnum is above lhrnum records are not moved during split or merge
	private int lhrnum; 
	
	/**
	 * @param keyType according to key value type
	 * @param set containing all possible key values
	 * @param bucketsCount initial number of buckets in index
	 * @param obligatory true if current key (record) requires to have value specified in index call
	 */
	public LHEnumRecordType(KeyType keyType, TreeSet set, int bucketsCount, boolean obligatory){
		super(obligatory);
		this.valCard = set.size();
		while (bucketsCount < valCard) bucketsCount <<= 1;
		while ((bucketsCount >> 1) >= valCard) bucketsCount >>= 1;
		this.lhrnum = bucketsCount;
		this.keyType = keyType;
		this.tempSet = set;
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public LHEnumRecordType(OID oid) throws DatabaseException {
		super(oid);
		valCard = getValuesRef().countChildren();
		lhrnum = getLHrnum();
	}

	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.LHENUMRECORDTYPE_ID;
	}
	
	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		OID valoid = store.createComplexObject(Names.KEYVALUES_ID, oid, valCard);
		for(Object value : tempSet)
			keyType.createValueObject(Names.VALUE_ID, valoid, value);		
		store.createIntegerObject(Names.LHRNUM_ID, oid, lhrnum);
	}
	
	private final int findNearestKeyValueIndex(Object value) {
		int L = 0;
		int R = valCard - 1;
		while (L <= R) {
			int index = (L + R) / 2;
			if (keyType.isLess(getValueAt(index), value))
				L = index + 1;
			else if (keyType.isEqual(getValueAt(index), value))
				return index;
			else R = index - 1;
		}
		return L;
	}
	
	@Override
	protected int rangeSelect(Object value, int rnum) {
		if (isBelowMin(value)) return 0;
		else if (isEqualAboveMax(value)) {
			if (rnum >= lhrnum) return valCard - 1;
			return rnum - 1;		
		}
		if (rnum >= lhrnum) rnum = lhrnum;
		
		return (findNearestKeyValueIndex(value) * rnum) / lhrnum;
	}
	
	protected int hash(int range, int bitnum, int rnum) {
		while (rnum > lhrnum) {
			bitnum--;
			rnum >>= 1;
		}
		return invBits(range % (1 << bitnum), bitnum) * (rnum >> bitnum) 
		+ range / (1 << bitnum);
	}
	
	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {
		if (isRangeQuery(keyValue)) {
			Object[] range = ((Object[])((Object[])keyValue)[1]);
			int bitnum = getLHLevel(rnum);					
			int rangecur = rangeSelect(((Object[]) range)[0], rnum);
			int rangemax = rangeSelect(((Object[]) range)[1], rnum);
			
			if (rnum >= valuesCardinality()) {
				if ((!(Boolean) ((Object[]) range)[2]) ||
						(keyType.isLess(getValueAt(rangecur), ((Object[]) range)[0]))) 
					rangecur++;
				if ((!(Boolean) ((Object[]) range)[3]) &&
						!(keyType.isLess(getValueAt(rangemax), ((Object[]) range)[1])))
					rangemax--;
			}
			
			int rcount = rangemax - rangecur + 1;
			if (rcount < 0) rcount = 0;
			HashSet<Integer> set = new HashSet<Integer>(rcount);
			while (rangecur <= rangemax) {
				set.add(hash(rangecur, bitnum, rnum));
				rangecur++;
			}
			return set;
		}
		
		return super.rangeHash(keyValue, rnum);
	}
		
	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
		if (rnum >= valCard)
			return new HashSet<Integer>();
		return super.limitHash(x, rnum);
	}
	
	@Override
	public boolean isEnumRecordType() {
		return true;
	}
	
	@Override
	protected int cardinality() {
		return valCard;
	}
	
	private void adjustLHrnum() throws DatabaseException {
		while (this.lhrnum < valCard) this.lhrnum <<= 1;
		while ((this.lhrnum >> 1) >= valCard) this.lhrnum >>= 1;
		getLHrnumRef().updateIntegerObject(this.lhrnum);
	}

	@Override
	public boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException {
		int index = findNearestKeyValueIndex(newKeyValue);
		if ((index == valCard) || !keyType.isEqual(getValueAt(index), newKeyValue)) {
			OID valoid = this.getValuesRef();	
			keyType.createValueObject(Names.VALUE_ID, valoid, getValueAt(valCard - 1));
			for(int i = valCard - 1; i > index; i--)
				keyType.updateValueObject(valoid.getChildAt(i), getValueAt(i - 1));
			keyType.updateValueObject(valoid.getChildAt(index), newKeyValue);
			valCard++;
			min = getMin();
			max = getMax();
			adjustLHrnum();
			return true;
		}	
		return false;
	}
	
	public Object filterKeyValue(Object keyValue) {
		if (isRangeQuery(keyValue))
			return keyValue;
		
		if (isInQuery(keyValue))
			return super.filterKeyValue(keyValue);
					
		int index = findNearestKeyValueIndex(keyValue);
		if ((index != valCard) && keyType.isEqual(getValueAt(index), keyValue))
			return keyValue;
		else
			return new Object[] { KeyType.IN_KEY, new Object[0] }; 	
	}
	
	@Override
	public Object getMin() throws DatabaseException {
		return getValueAt(0);
	}
	
	@Override
	public Object getMax() throws DatabaseException {
		return getValueAt(getValuesRef().countChildren() - 1);
	}
	
	protected Object getValueAt(int index) {
		try {
			return keyType.OIDToValue(getValuesRef().getChildAt(index));
		} catch (DatabaseException e) {
			//TODO: proper exceptions handeling
			return -1;	
		}  
	}
	
	private int getLHrnum() throws DatabaseException {
		return getLHrnumRef().derefInt();
	}
	
	protected OID getValuesRef() throws DatabaseException {
		return oid.getChildAt(VALUES_POS);
	}
			
	private OID getLHrnumRef() throws DatabaseException {
		return oid.getChildAt(LHRNUM_POS);
	}
	
	private final static int VALUES_POS = HASHSEED_POS + 1;
	private final static int LHRNUM_POS = VALUES_POS + 1;

}
