package odra.db.indices.keytypes;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.NonkeyIndexRegister;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;

/**
 * 
 * This class provides methods used for indexing objects by combination of:
 * <li>SBQL AST key values dumped to string SBQL queries.</li><br>
 * <li>OID to MBObject describing indexed variable.</li><br>
 * It is used to index existing indices to perform quick optimization during compile-time
 * and to maintain index register.
 * 
 * @author tkowals
 * @version 1.0
 */
public class IndexUniqueNKKeyType extends KeyType {

	StringKeyType stringKeyType = new StringKeyType();
	
	@Override
	public int getKeyTypeID() {
		return KeyTypeKind.INDEXUNIQUENKKEYTYPE_ID;
	}

	@Override
	public boolean isEqual(Object keyValue, Object cmpKeyValue) {
		if (((DefaultStoreOID) ((Object[])keyValue)[1]).getOffset() != ((DefaultStoreOID) ((Object[])cmpKeyValue)[1]).getOffset())
			return false;
		
		String xast = (String) ((Object[])keyValue)[0];
		String yast = (String) ((Object[])cmpKeyValue)[0];
		
		return stringKeyType.isEqual(xast, yast);
	}
	
	@Override
	public boolean isLess(Object keyValue, Object cmpKeyValue) {
		if (((DefaultStoreOID) ((Object[])keyValue)[1]).getOffset() < ((DefaultStoreOID) ((Object[])cmpKeyValue)[1]).getOffset())
			return true;
		
		String xast = (String) ((Object[])keyValue)[0];
		String yast = (String) ((Object[])cmpKeyValue)[0];

		return stringKeyType.isLess(xast, yast);
	}
	
	@Override
	public int hash(Object keyValue, int rnum) {
		
		int hash = ((DefaultStoreOID) ((Object[])keyValue)[1]).getOffset() % rnum;
		
		String xast = (String) ((Object[])keyValue)[0];
			
		return hash + stringKeyType.hash(xast, rnum) + rnum;
		
	}

	@Override
	public Object byteBuffer2KeyValue(ByteBuffer buffer) throws DatabaseException {
		return key2KeyValueDirectly(new DefaultStoreOID(buffer.getInt(), (DefaultStore) dataAccess.getStore()));
	}

	@Override
	public byte[] key2Array(Object key) throws DatabaseException {		
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}

	@Override
	public Object key2KeyValueDirectly(Object key) throws DatabaseException {
		OID unkoid = (OID) key;
		return new Object[] {NonkeyIndexRegister.getUNKAst(unkoid), NonkeyIndexRegister.getUNKVar(unkoid)};
	}

	@Override
	public Object key2KeyValue(Object key) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}
	
	@Override
	public void createValueObject(int name, OID parentoid, Object value) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}
	
	@Override
	public Object OIDToValue(OID oid) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}
	
	@Override
	public void updateValueObject(OID oid, Object value) throws DatabaseException {
		throw new DatabaseException("Wrong recordtype for indexing unique nonkeys!");
	}
	
}
