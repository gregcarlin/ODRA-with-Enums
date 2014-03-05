package odra.db.indices.dataaccess;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.DefaultStore;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides indexing for OIDs.
 * Single index record contain following
 * | Object OID |<br>
 * <br>
 * Used with IndexUniqueNKKeyType for indexing indices nonkeys.
 * <br><br>
 * OIDAccess complex object structure:<ul>
 * <li> OIDACCESS_ID </li>
 * </ul> 
 * 
 * @author tkowals
 * @version 1.0
 */

public class OIDAccess extends DataAccess {

	/**
	 * Creates OIDAccess 
	 * serialized in complex object given by oid parameter.
	 * @param oid complex object containing dataaccess description
	 */
	public OIDAccess(OID oid) {
		super(oid);
	}
	
	/**
	 * Creates uninitialized OIDAccess. 
	 */
	public OIDAccess() {

	}
	
	@Override
	public Object key2keyValue(Object key) throws DatabaseException {
		return recordType.keyType.key2KeyValueDirectly(key);
	}	
	
	@Override
	public Object record2nonkey(byte[] record) {
		return new DefaultStoreOID(ByteBuffer.wrap(record).getInt(), (DefaultStore) store);
	}
	
	@Override
	public Object nonkey2key(Object nonkey) throws DatabaseException {
		assert false:"unimlemented unused?";
		return nonkey;
	}
	
	@Override
	public byte[] prepareRecordArray(Object key, Object nonkey) throws DatabaseException {
		return ByteBuffer.allocate(getNonkeyRecordSize()).putInt(((DefaultStoreOID) nonkey).getOffset()).array();
	}

	@Override
	public Object record2keyValue(byte[] record) throws DatabaseException { 		
		return recordType.keyType.byteBuffer2KeyValue((ByteBuffer) ByteBuffer.wrap(record));
	}			
	
	@Override
	protected int getKindID() {
		return DataAccessKind.OIDACCESS_ID;
	}
	
	/**
	 * @return 4 - in case of OIDAccess individual record stored in index consist of one int value 
	 */
	public int getNonkeyRecordSize() {
		return Sizes.INTVAL_LEN;
	}
	
	/**
	 * @return null - default value returned by index if nonkey has not been found
	 */
	public Object getNotFoundValue() {
		return null;
	}
}
