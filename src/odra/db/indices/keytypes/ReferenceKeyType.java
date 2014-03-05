package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;

import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing objects by OID key values.<br>
 * Key can wrap key value by ReferenceResult.<br>
 * Key can in some cases represent set of key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class ReferenceKeyType extends KeyType {

	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.REFERENCEKEYTYPE_ID;
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
		return ByteBuffer.allocate(Sizes.INTVAL_LEN).putInt((Integer) key2KeyValueDirectly(key)).array();
	}	
	
	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		if (key instanceof ReferenceResult) { 
			ReferenceResult refres = (ReferenceResult) key;
			if (refres.value.isReferenceObject())
				key = new ReferenceResult(refres.value.derefReference());
		}
		return ((DefaultStoreOID) ((ReferenceResult) key).value).getOffset();
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}

	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}
	
}
