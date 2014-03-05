package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.StringResult;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by string key values.<br>
 * Key can wrap key value (StringResult or ReferenceResult).<br>
 * Key can in some cases represent set of key values or range of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class StringKeyType extends KeyType {

	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.STRINGKEYTYPE_ID;
	}

	//	 djb2 string hashing
	@Override
	public int hash(Object keyValue, int rnum) {
		String str = (String) keyValue;
		
		int hash = 777;
		for (int i = 0; i < str.length(); i++) {
			hash = (((hash << 5) + hash) + str.charAt(i)) % rnum;			
		}
		
		return hash + rnum;
	}

	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		return ((String) keyValue).compareTo((String) cmpKeyValue) < 0;
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		return new DefaultStoreOID(buffer.getInt(), (DefaultStore) dataAccess.getStore()).derefString();
	}
	
	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt(((DefaultStoreOID) ((ReferenceResult) key).value).getOffset()).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return ((ReferenceResult) key).value.derefString();
	}
	
	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		if (key instanceof StringResult) return ((StringResult) key).value;
		return super.key2KeyValue(key);
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		parentoid.createStringChild(name, (String) value, 0);
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		return oid.derefString();
	}
	
	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		oid.updateStringObject((String) value);
	}
	
}
