package odra.db.indices.recordtypes;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.KeyType;
import odra.db.indices.recordtypes.RecordType;

/**
 * This class makes support for non-enum and non-ordered objects indexing.<br>
 * Used for indexing NameIndex and for dense indices.<br>
 * <p>Non-ordered and non-enum record type has following properties:<ul>
 * <li>range queries are not supported</li>
 * <li>is obligatory because hash function span all possible values</li>
 * <li>2 different values can have identical hash value for given number of buckets in LH structure rnum (if it is to low) or regardless LH buckets number (rnum)</li>
 * <li>more buckets in LH structure (rnum) more different possible hash values. Hash values can be from range 0 .. rnum-1</li>
 * </ul></p>
 * <p>Simple record type complex object structure:<ul>
 * <li> ID of record type (SIMPLERECORDTYPE_ID)</li>
 * <li> ID of key type (according to KeyTypeKind "enumeration")</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */

public class SimpleRecordType extends RecordType {

	/**
	 * @param keyType according to key value type
	 */
	public SimpleRecordType(KeyType keyType) {
		super(true);
		this.keyType = keyType;
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public SimpleRecordType(OID oid) throws DatabaseException {
		super(oid);
	}
	
	@Override
	public void initialize(OID oid) throws DatabaseException {
		super.initialize(oid);
	}
	
	/** 
	 * Method not used 
	 */
	public Object getMin() throws DatabaseException {
		assert false : "getMin not applicable for non-ordered and non-enum type"; 
		return null;
	}
	
	/** 
	 * Method not used 
	 */
	public Object getMax() throws DatabaseException {
		assert false : "getMax not applicable for non-ordered and non-enum type";
		return null;
	}
	
	@Override
	public int hash(Object x, int rnum) {
		return keyType.hash(x, rnum);
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.SIMPLERECORDTYPE_ID;
	}

	@Override
	public boolean isOrderedRecordType() {
		return false;
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
