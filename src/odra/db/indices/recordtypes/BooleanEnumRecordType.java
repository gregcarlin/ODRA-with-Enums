package odra.db.indices.recordtypes;

import java.util.HashSet;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.keytypes.BooleanKeyType;

/**
 * This is an enum and non-ordered record type.<br> 
 * <br>
 * <p>In case this class enum record type has following properties:<ul>
 * <li>key have limited number of distinct values (valCard)</li> 
 * <li>only two distinct key values are possible TRUE or FALSE and therefore two different hash values</li><br>
 * </ul></p>
 * NOTE: Should not be uses as single key index<br>
 * <p>Boolean enum record type complex object structure:<ul>
 * <li> ID of record type (BOOLEANENUMRECORDTYPE_ID)</li>
 * <li> ID of key type (BOOLEANKEYTYPE_ID)</li>
 * <li> indicates if current key (record) requires to have value specified in index call or if key value can be omitted</li>
 * <li> <b>hashseed</b> for current key hashfunction</li>
 * </ul></p>
 * @author tkowals
 * @version 1.0
 */
public class BooleanEnumRecordType extends RecordType {

	/**
	 * @param obligatory true if current key (record) requires to have value specified in index call
	 */
	public BooleanEnumRecordType(boolean obligatory) {
		super(obligatory);
		keyType = new BooleanKeyType();
	}
	
	/**
	 * @param oid complex object containing recordtype description
	 * @throws DatabaseException
	 */
	public BooleanEnumRecordType(OID oid) throws DatabaseException {
		super(oid);
	}
	
	@Override
	public int getRecordTypeID() {
		return RecordTypeKind.BOOLEANENUMRECORDTYPE_ID;
	}

	@Override
	public Object getMin() throws DatabaseException {
		throw new DatabaseException("Unsupported operation on boolean key!");
	}
	
	@Override
	public Object getMax() throws DatabaseException {
		throw new DatabaseException("Unsupported operation on boolean key!");
	}
	
	@Override
	public int hash(Object x, int rnum) {
		return keyType.hash(x, rnum);
	}
	
	@Override
	public HashSet<Integer> rangeHash(Object keyValue, int rnum) {
		if (keyValue instanceof Object[]) {
			HashSet<Integer> set = new HashSet<Integer>(2);
			set.add(hash(false, rnum));
			set.add(hash(true, rnum));
			return set;
		}
		
		return super.rangeHash(keyValue, rnum);
	}
	
	@Override
	public HashSet<Integer> limitHash(Object x, int rnum) {
		return new HashSet<Integer>();
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
		return 2;
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
