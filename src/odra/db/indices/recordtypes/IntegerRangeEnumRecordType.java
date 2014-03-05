package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.IntegerKeyType;
import odra.db.indices.keytypes.KeyType;
import odra.system.Names;

/**
 * This is a enum and non-ordered record type.<br>
 * <p>Integer range enum record type has following properties:<ul>
 * <li>key have limited number of distinct values (valCard)</li>
 * <li>range queries are supported</li> 
 * <li>in case if number LH buckets is smaller then key values cardinality (rnum < valCard) then hash can take all possible values 0 .. rnum-1</li>
 * <li>2 diffrent values can have identical hash value (rnum < valCard) </li>
 * <li>when LH buckets number gets larger then key values cardinality (valCard <= rnum) then:</li> 
 * <ul>
 * <li>hash takes valCard distinct values and 2 different values have different hash value. </li>
 * <li>smaller value means smaller hash.</li>
 * </ul></ul></p>
 * <p>Integer range record type complex object structure:<ul>
 * <li> ID of record type (INTEGERRANGEENUMRECORDTYPE_ID)</li>
 * <li> ID of key type (INTEGERKEYTYPE_ID)</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>min</b> minimal indexed key value</li>
 * <li> <b>max</b> maximum indexed key value</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class IntegerRangeEnumRecordType extends RecordType {

	private int min, max;
	
	/**
	 * @param min minimal indexed key value
	 * @param max maximum indexed key value
	 */
	public IntegerRangeEnumRecordType(int min, int max) {
		super(false);
		this.min = min;
		this.max = max;
		keyType = new IntegerKeyType();
	}

	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public IntegerRangeEnumRecordType(OID oid) throws DatabaseException {
		super(oid);
		min = (Integer) getMin();
		max = (Integer) getMax();
	}

	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		keyType.createValueObject(Names.MIN_CARD_ID, oid, min);
		keyType.createValueObject(Names.MAX_CARD_ID, oid, max);
	}
	
	@Override
	public int hash(Object x, int rnum) {
		return rangeSelect(x, rnum);
	}
	
	protected int rangeSelect(Object value, int rnum) {
		if ((Integer) value < min) return 0;
		else if ((Integer) value >= max) return max - min;		
		return ((Integer) value - min);
	}

	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {
		if (isRangeQuery(keyValue)) {
			Object[] range = ((Object[])((Object[])keyValue)[1]);
			int rangecur = rangeSelect(((Object[]) range)[0], rnum);
			int rangemax = rangeSelect(((Object[]) range)[1], rnum);
			
			// TODO: TEST NEXT LINES (Compare to EnumRecordType)
			if (rnum >= valuesCardinality()) {
//	OLD			if (!(Boolean) ((Object[]) range)[2]) rangecur++;
//	OLD			if (!(Boolean) ((Object[]) range)[3]) rangemax--;
				if ((!(Boolean) ((Object[]) range)[2]) ||
						(keyType.isLess(rangecur + min, ((Object[]) range)[0]))) 
					rangecur++;
				if ((!(Boolean) ((Object[]) range)[3]) &&
						!(keyType.isLess(rangemax + min, ((Object[]) range)[1])))
					rangemax--;
			}
		
			int rcount = rangemax - rangecur + 1;
			if (rcount < 0) rcount = 0;
			HashSet<Integer> set = new HashSet<Integer>(rcount);
			while (rangecur <= rangemax) {
				set.add(rangecur);
				rangecur++;
			}
			return set;
		}
		
		return super.rangeHash(keyValue, rnum);
	}
	
	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
		if (rnum >= valuesCardinality())
			return new HashSet<Integer>();
		return super.limitHash(x, rnum);
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.INTEGERRANGEENUMRECORDTYPE_ID;
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
	protected int cardinality() {
		return max - min + 1;
	}
	
	private void setMin(Object newMin) throws DatabaseException {
		getMinRef().updateIntegerObject((Integer) newMin);
		min = (Integer) newMin;
	}

	private void setMax(Object newMax) throws DatabaseException {
		getMaxRef().updateIntegerObject((Integer) newMax);
		max = (Integer)newMax;
	}
	
	@Override
	public boolean adjust2KeyValue(Object newKeyValue) throws DatabaseException {
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
	
	@Override
	public Object getMin() throws DatabaseException {
		return getMinRef().derefInt();
	}
	
	@Override
	public Object getMax() throws DatabaseException {
		return getMaxRef().derefInt();
	}

	protected OID getMinRef() throws DatabaseException {
		return oid.getChildAt(MIN_POS);
	}
	
	protected OID getMaxRef() throws DatabaseException {
		return oid.getChildAt(MAX_POS);
	}
	
	private final static int MIN_POS = HASHSEED_POS + 1;
	private final static int MAX_POS = MIN_POS + 1;
	
}
