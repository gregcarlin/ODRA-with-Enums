package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.StructResult;

/**
 * 
 * This class provides methods used for indexing objects by multiple key values.<br>
 * Key consists of a group of individual key values.
 * 
 * @author tkowals
 * @version 1.0
 */
public class MultiKeyType extends KeyType {

	KeyType[] keyType;
	
	/**
	 * @param keyType array of singular key types
	 */
	public MultiKeyType(KeyType[] keyType) {
		super();
		this.keyType = keyType; 
	}
	
	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.MULTIKEYTYPE_ID;
	}

	@Override
	public int hash(Object keyValue, int rnum) {
		int hash = 0;
		for (int i = 0; i < keyType.length; i++)
			hash = (hash + keyType[i].hash(((Object[]) keyValue)[i], rnum)) % rnum;
	
		return hash + rnum;
	}

	@Override
	public boolean isEqual(Object keyValue, Object y) {
		for (int i = 0; i < keyType.length; i++) {
			if (!(keyType[i].isEqual(((Object[]) keyValue)[i], ((Object[]) y)[i])))
				return false;
		}
		return true;				
	}	
	
	@Override
	public boolean isInKeyValue(Object keyValue, Object cmpKeyValue) {
		for (int i = 0; i < keyType.length; i++) {
			if (!(keyType[i].isInKeyValue(((Object[]) keyValue)[i], ((Object[]) cmpKeyValue)[i])))
				return false;
		}
		return true;				
	}
	
	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue){
		for (int i = 0; i < keyType.length; i++) {
			if (keyType[i].isLess(((Object[]) keyValue)[i], ((Object[]) cmpKeyValue)[i]))
				return true;
			if (!keyType[i].isEqual(((Object[]) keyValue)[i], ((Object[]) cmpKeyValue)[i]))
				return false;
		}
		return false;
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		throw new DatabaseException("Storing multikeys in index is forbidden!");
	}
	
	@Override
	public byte[] key2Array(Object key) throws DatabaseException {
		throw new DatabaseException("Storing multikeys in index is forbidden!");
	}

	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		throw new DatabaseException("Storing multikeys in index is forbidden!");
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
	
		Object[] keyValue;
		if (key instanceof BagResult) {
			BagResult bres = (BagResult) key;
			keyValue = new Object[bres.elementsCount()];
			for (int i = 0 ; i < bres.elementsCount(); i++)
				keyValue[i] = keyType[i].key2KeyValue(bres.elementAt(bres.elementsCount() - i - 1));
			
		} else {
			StructResult stres = (StructResult) key;
			keyValue = new Object[stres.fieldsCount()];
			for (int i = stres.fieldsCount() - 1; i >= 0; i--)
				keyValue[i] = keyType[i].key2KeyValue(stres.fieldAt(i));
		}
		
		return keyValue;
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		throw new DatabaseException("Storing multikeys as single index key is forbidden!");
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		throw new DatabaseException("Storing multikeys as single index key is forbidden!");
	}
	
	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		throw new DatabaseException("Storing multikeys as single index key is forbidden!");
	}
	
}
