package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.NonkeyIndexRegister;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;

/**
 * 
 * This class provides methods used for indexing objects 
 * by SBQL AST key values dumped to string SBQL queries.<br>
 * It is used to index existing indices to perform quick optimization during compile-time 
 * and to maintain index register.
 * 
 * @author tkowals
 * @version 1.0
 */
public class ASTTextKeyType extends StringKeyType {

	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.ASTTEXTKEYTYPE_ID;
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		return NonkeyIndexRegister.getUniqueKeyAst(new DefaultStoreOID(buffer.getInt(), (DefaultStore) dataAccess.getStore()));
	}
	
	@Override
	public byte[] key2Array(Object key) throws DatabaseException {		
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}

	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		return key;
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique keys!");
	}
}
