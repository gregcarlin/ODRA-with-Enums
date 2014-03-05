package odra.db.indices.recordtypes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.DoubleKeyType;

/**
 * This is a non-enum and ordered record type.<br>
 * <p>Ordered record type has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>is obligatory because hash function span all possible values</li>
 * </ul></p>
 * <p>Double LH range record type complex object structure:<ul>
 * <li>ID of record type (DOUBLELHRANGERECORDTYPE_ID)</li>
 * <li>ID of key type (DOUBLEKEYTYPE_ID)</li>
 * <li>indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li><b>hashseed</b> for current key hashfunction</li>
 * <li><b>min</b> double to specify range of partitioning in index</li>
 * <li><b>max</b> double to specify range of partitioning in index</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class DoubleLHRangeRecordType extends LHRangeRecordType {

	private static final double EPSILON = 0.0000000001;
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public DoubleLHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
	}
	
	/**
	 * @param min double to specify range of partitioning in index
	 * @param max double to specify range of partitioning in index
	 */
	public DoubleLHRangeRecordType(double min, double max) {
		this.min = Double.valueOf(min);
		this.max = Double.valueOf(max);
		keyType = new DoubleKeyType();
	}

	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
	}	
	
	@Override
	protected final int rangeSelect(Object value, int rnum) {
		if (isBelowMin(value)) return 0;
		else if (isEqualAboveMax(value)) return rnum - 1;			
		//TODO: Check range selection! Is Epsilon working?
		return (int) Math.floor((((Double) value - (Double) min) * rnum) / ((Double) max - (Double) min + EPSILON));
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.DOUBLELHRANGERECORDTYPE_ID;
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
	
}
