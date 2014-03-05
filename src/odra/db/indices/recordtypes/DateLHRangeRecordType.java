package odra.db.indices.recordtypes;

import java.util.Date;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.DateKeyType;

/**
 * This is a non-enum and ordered record type.<br>
 * <p>Ordered record type has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>is obligatory because hash function span all possible values</li>  
 * </ul></p>
 * <p>Date LH range record type complex object structure:<ul>
 * <li> ID of record type (DATELHRANGERECORDTYPE_ID)</li>
 * <li> ID of key type (DATEKEYTYPE_ID)</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>min</b> date to specify range of partitioning in index</li>
 * <li> <b>max</b> date to specify range of partitioning in index</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class DateLHRangeRecordType extends LHRangeRecordType {
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public DateLHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
	}
	
	/**
	 * @param min date to specify range of partitioning in index
	 * @param max date to specify range of partitioning in index
	 */
	public DateLHRangeRecordType(Date min, Date max) {
		super();
		this.min = min;
		this.max = max;
		keyType = new DateKeyType();
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
		return (int) (((((Date) value).getTime() - ((Date) min).getTime()) * rnum) / (((Date) max).getTime() - ((Date)min).getTime() + 1));
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.DATELHRANGERECORDTYPE_ID;
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
