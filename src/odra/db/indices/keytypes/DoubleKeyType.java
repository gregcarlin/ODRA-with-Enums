package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by double key values.<br>
 * Key can wrap key value (DoubleResult or ReferenceResult).<br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class DoubleKeyType extends KeyType {
	
	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.DOUBLEKEYTYPE_ID;
	}
	
	@Override
	public int hash(Object keyValue, int rnum) {
		long v = Double.doubleToLongBits((Double) keyValue);
		int hash = (int)(v^(v>>32)) % rnum;

		return hash + rnum;
	}

	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		return (Double) keyValue < (Double) cmpKeyValue;
	}
	
	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		return new DefaultStoreOID(buffer.getInt(), (DefaultStore) dataAccess.getStore()).derefDouble();
	}
	
	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt(((DefaultStoreOID) ((ReferenceResult) key).value).getOffset()).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return ((ReferenceResult) key).value.derefDouble();
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof DoubleResult) return ((DoubleResult) key).value;
		return super.key2KeyValue(key);
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		parentoid.createDoubleChild(name, (Double) value);
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		return oid.derefDouble();
	}
	
	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		oid.updateDoubleObject((Double) value);
	}

}
