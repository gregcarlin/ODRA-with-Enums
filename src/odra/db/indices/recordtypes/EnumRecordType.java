package odra.db.indices.recordtypes;

import java.util.HashSet;
import java.util.TreeSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.KeyType;
import odra.system.Names;

/**
 * This is a enum and non-ordered record type.<br>
 * <p>Enum record type has following properties:<ul>
 * <li>key have limited number of distinct values (valCard)</li>
 * <li>range queries are supported</li> 
 * <li>in case if number LH buckets is smaller then key values cardinality (rnum < valCard) then hash can take all possible values 0 .. rnum-1</li>
 * <li>2 diffrent values can have identical hash value (rnum < valCard) </li>
 * <li>when LH buckets number gets larger then key values cardinality (valCard <= rnum) then:</li> 
 * <ul>
 * <li>hash takes valCard distinct values and 2 different values have different hash value. </li>
 * <li>smaller value means smaller hash.</li>
 * </ul></ul></p>
 * NOTE: Should not be uses as single key index if values cardinality is small (i.e. smaller then 128)!
 * <p>Enum range record type complex object structure:<ul>
 * <li> ID of record type (ENUMRECORDTYPE_ID)</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>keyvalues</b> complex object containing all possible keyvalues ordered
 * <ul>
 * <li> minimal indexed key value</li>
 * <li> ... ordered key values
 * <li> maximum indexed key value</li>
 * </ul></li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class EnumRecordType extends RecordType {

	Object min, max;
	
	int valCard;
	
	TreeSet tempSet;
	
	/**
	 * @param keyType according to key value type
	 * @param set containing all possible key values
	 * @param obligatory true if current key (record) requires to have value specified in index call
	 */
	public EnumRecordType(KeyType keyType, TreeSet set, boolean obligatory){		
		super(obligatory);
		this.valCard = set.size();
		this.keyType = keyType;
		this.tempSet = set;
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public EnumRecordType(OID oid) throws DatabaseException {
		super(oid);
		valCard = getValuesRef().countChildren();
		min = getMin();
		max = getMax();
	}

	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.ENUMRECORDTYPE_ID;
	}
	
	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		OID valoid = store.createComplexObject(Names.KEYVALUES_ID, oid, valCard);
		for(Object value : tempSet)
			keyType.createValueObject(Names.VALUE_ID, valoid, value);
		min = getMin();
		max = getMax();
	}

	protected final boolean isBelowMin(Object value) {
		return keyType.isLess(value, min);
	}
	
	protected final boolean isEqualAboveMax(Object value){
		return (keyType.isLess(max, value) || keyType.isEqual(max, value));
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
	
	protected int rangeSelect(Object value, int rnum) {
		if (isBelowMin(value)) return 0;
		else if (isEqualAboveMax(value)) return valCard - 1;
		
		return findNearestKeyValueIndex(value);
	}
	
	@Override
	public int hash(Object x, int rnum) {
		return rangeSelect(x, rnum);
	}

	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {
		if (isRangeQuery(keyValue)) {
			Object[] range = ((Object[])((Object[])keyValue)[1]);
			int rangecur = rangeSelect(((Object[]) range)[0], rnum);
			int rangemax = rangeSelect(((Object[]) range)[1], rnum);
			
			// TODO: test if (rnum >= valuesCardinality()) necessary
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
				set.add(rangecur);
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
	public boolean isOrderedRecordType() {
		return false;
	}
	
	@Override
	protected int cardinality() {
		return valCard;
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
	
	protected OID getValuesRef() throws DatabaseException {
		return oid.getChildAt(VALUES_POS);
	}
	
	private final static int VALUES_POS = HASHSEED_POS + 1;

}
