package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import java.util.Date;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.DateResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by date key values.<br>
 * Key can wrap key value (DateResult or ReferenceResult).<br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class DateKeyType extends KeyType {
	
	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.DATEKEYTYPE_ID;
	}
	
	@Override
	public int hash(Object keyValue, int rnum) {
		return (((Date) keyValue).hashCode() % rnum) + rnum;
	}

	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		return ((Date) keyValue).compareTo((Date) cmpKeyValue) < 0;
	}
	
	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		return new DefaultStoreOID(buffer.getInt(), (DefaultStore) dataAccess.getStore()).derefDate();
	}
	
	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt(((DefaultStoreOID) ((ReferenceResult) key).value).getOffset()).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return ((ReferenceResult) key).value.derefDate();
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof DateResult) return ((DateResult) key).value;
		return super.key2KeyValue(key);
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		parentoid.createDateChild(name, (Date) value);
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		return oid.derefDate();
	}

	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		oid.updateDateObject((Date) value);
	}

}
