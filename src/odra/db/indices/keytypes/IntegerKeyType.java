package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by integer key values.<br>
 * Key can wrap key value (IntegerResult or ReferenceResult).<br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IntegerKeyType extends KeyType {

	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.INTEGERKEYTYPE_ID;
	}

	@Override
	public int hash(Object keyValue, int rnum) {
		return (Integer) keyValue % rnum + rnum;
	}

	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		return (Integer) keyValue < (Integer) cmpKeyValue;
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) {
		return buffer.getInt();
	}

	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt((Integer) key2KeyValue(key)).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return ((ReferenceResult) key).value.derefInt();
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof IntegerResult) return ((IntegerResult) key).value; 
		return super.key2KeyValue(key);
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		parentoid.createIntegerChild(name, (Integer) value);
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		return oid.derefInt();
	}

	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		oid.updateIntegerObject((Integer) value);
	}
}
