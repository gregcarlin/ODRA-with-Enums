package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by boolean key values.<br>
 * Key can wrap key value (Boolean Result or ReferenceResult).<br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class BooleanKeyType extends KeyType {

	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.BOOLEANKEYTYPE_ID;
	}

	@Override
	public int hash(Object keyValue, int rnum) {
		return (Boolean) keyValue? 1: 0;
	}

	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		return false;
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) {
		return buffer.getInt() == 1;
	}

	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt((Boolean) key2KeyValueDirectly(key)?1:0).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return ((ReferenceResult) key).value.derefBoolean();
	}
		
	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof BooleanResult) return ((BooleanResult) key).value; 
		return super.key2KeyValue(key);
	}	

	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		parentoid.createBooleanChild(name, (Boolean) value);
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		return oid.derefBoolean();
	}

	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		oid.updateBooleanObject((Boolean) value);
	}
	
}
