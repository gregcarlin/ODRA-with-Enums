package odra.db.indices.recordtypes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.StringKeyType;

/**
 * This is a non-enum and ordered record type.<br>
 * <p>Ordered record type has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>is obligatory because hash function span all possible values</li>
 * </ul></p>
 * <p>String LH range record type complex object structure:<ul>
 * <li> ID of record type (STRINGLHRANGERECORDTYPE_ID)</li>
 * <li> ID of key type (STRINGKEYTYPE_ID)</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>min</b> string to specify range of partitioning in index</li>
 * <li> <b>max</b> string to specify range of partitioning in index</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class StringLHRangeRecordType extends LHRangeRecordType {

	private int start;
	private long minvalue, maxvalue;
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public StringLHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
		
		setHelpers();
		
	}
	
	/**
	 * @param min string to specify range of partitioning in index
	 * @param max string to specify range of partitioning in index
	 */
	public StringLHRangeRecordType(String min, String max) {
		this.min = min;
		this.max = max;
		keyType = new StringKeyType();
		
		setHelpers();
	}

	private void setHelpers() {
		start = 0;
		while(valueOfCharAt((String) min, start) == valueOfCharAt((String) max, start))
			start++;
		maxvalue = 0;
		for(int i = start; i < start + 4; i++) {
			minvalue = stringToValue((String) min);
			maxvalue = stringToValue((String) max);
		}		
	}
	
	@Override
	public void initialize(OID OID) throws DatabaseException {
		super.initialize(OID);
	}	
	
	@Override
	public int hash(Object x, int rnum) {
		return hash(rangeSelect(x, rnum), getLHLevel(rnum), rnum);
	}
	
	@Override
	protected final int rangeSelect(Object value, int rnum) {
		if (isBelowMin(value)) return 0;
		else if (isEqualAboveMax(value)) return rnum - 1;		
		
		long valueint = stringToValue((String) value);
		
		return (int) (((valueint - minvalue) * rnum) / (maxvalue - minvalue + 1));
	}

	private int stringToValue(String str) {
		int value = 0;
		for(int i = start; i < start + 4; i++)
			value = (value * (MAX_CHAR - MIN_CHAR + 1)) + valueOfCharAt(str, i);
		return value;	
	}
	
	private int valueOfCharAt(String str, int index) {
		if (index < str.length()) {
			int value = (int) str.charAt(index);
			if (value > MAX_CHAR) 
				return MAX_CHAR - MIN_CHAR;
			if (value < MIN_CHAR)
				return 0;
			return value - MIN_CHAR;
		}
		return 0;
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.STRINGLHRANGERECORDTYPE_ID;
	}
	
	@Override
	public boolean isEnumRecordType() {
		return false;
	}
	
	@Override
	protected int cardinality() {
		return 0;
	}
	
	@Override
	public boolean adjust2KeyValue(Object newKeyValue) {
		return false;
	}
	
	@Override
	public Object filterKeyValue(Object keyValue) {
		return keyValue;
	}
	
	public final static int MAX_CHAR = 122;
	public final static int MIN_CHAR = 48;
	
}
