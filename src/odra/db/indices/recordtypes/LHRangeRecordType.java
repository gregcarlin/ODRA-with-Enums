package odra.db.indices.recordtypes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;

/**
 * This is a super class for ordered range record types.<br>
 * <p>Ordered range record types has following properties:<ul>
 * <li>2 different values can have identical hash value only if they are in enough close range in order (buckets divided by range objects)</li>
 * <li>range queries are supported</li>
 * <li>is obligatory when hash function span all possible values (non-enum record type)</li>
 * </ul></p>
 * <p>LH range record type complex object structure:<ul>
 * <li> ID of record type (according to RecordTypeKind "enumeration")</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * <li> <b>min</b> value to specify range of partitioning in index</li>
 * <li> <b>max</b> value to specify range of partitioning in index</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public abstract class LHRangeRecordType extends LHOrderedRecordType {

	/**
	 * Assumes that key value for this record is obligatory in index call
	 */
	public LHRangeRecordType() {
		super(true);
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public LHRangeRecordType(OID oid) throws DatabaseException {
		super(oid);
	}

	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
		//FIXME: Change names of objects below (to MIN_VALUE and MAX_VALUE)
		keyType.createValueObject(Names.MIN_CARD_ID, oid, min);
		keyType.createValueObject(Names.MAX_CARD_ID, oid, max);
	}	

	@Override
	public Object getMin() throws DatabaseException {
		return keyType.OIDToValue(getMinRef());
	}
	
	@Override
	public Object getMax() throws DatabaseException {
		return keyType.OIDToValue(getMaxRef());
	}

	protected OID getMinRef() throws DatabaseException {
		return oid.getChildAt(MIN_POS);
	}
	
	protected OID getMaxRef() throws DatabaseException {
		return oid.getChildAt(MAX_POS);
	}
	
	protected final static int MIN_POS = HASHSEED_POS + 1;
	protected final static int MAX_POS = MIN_POS + 1;
	
}
