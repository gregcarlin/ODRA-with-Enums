package odra.db.indices.dataaccess;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sbql.results.runtime.ReferenceResult;
import odra.store.DefaultStoreOID;
import odra.system.Sizes;

/**
 * 
 * This class provides methods used for indexing database objects by theirs attribute.
 * Single index record contain following<br>
 * | Object OID | Attributes OID |<br>
 * or in case of integer value<br>
 * | Object OID | integer value |<br>
 * <br>
 * NOTE: Should not be used with MultiKeyRecordType.
 * <br><br>
 * DBObjectDirectKeyAccess complex object structure:<ul>
 * <li> DBOBJDIRKEYACCESS_ID </li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */

public class DBObjectDirectKeyAccess extends DBObjectToKeyAccess {

	/**
	 * Creates DBObjectDirectKeyAccess 
	 * serialized in complex object given by oid parameter.
	 * This constructor should be used for SBQL transparent indices. 
	 * @param oid complex object containing dataaccess description
	 * @param module index call execution context module
	 * @throws DatabaseException 
	 */
	public DBObjectDirectKeyAccess(OID oid, DBModule module) throws DatabaseException {
		super(oid, module);
	}
	
	/**
	 * Creates DBObjectDirectKeyAccess 
	 * serialized in complex object given by oid parameter.
	 * @param oid complex object containing dataaccess description
	 * @throws DatabaseException 
	 */
	public DBObjectDirectKeyAccess() {
		super();
	}
	
	/**
	 * Creates uninitialized DBObjectDirectKeyAccess. 
	 * @throws DatabaseException 
	 */
	public DBObjectDirectKeyAccess(byte[] genKeyCode, byte[] cnstPool) throws DatabaseException {
		super(genKeyCode, cnstPool);
	}

	@Override
	public byte[] prepareRecordArray(Object key, Object nonkey) throws DatabaseException {
		return ByteBuffer.allocate(getNonkeyRecordSize()).putInt(((DefaultStoreOID) ((ReferenceResult) nonkey).value).getOffset()).put(recordType.keyType.key2Array(key)).array();
	}

	@Override
	public Object record2keyValue(byte[] record) throws DatabaseException { 
		return recordType.keyType.byteBuffer2KeyValue((ByteBuffer) ByteBuffer.wrap(record).position(Sizes.INTVAL_LEN));
	}			
	
	@Override
	protected int getKindID() {
		return DataAccessKind.DBOBJDIRKEYACCESS_ID;
	}
	
	/**
	 * @return 8 - in case of DBObjectDirectKeyAccess individual record stored in index consist of two int values 
	 */
	public int getNonkeyRecordSize() {
		return 2 * Sizes.INTVAL_LEN;
	}
	
	/**
	 * @return null - default value returned by index if nonkey has not been found
	 */
	public Object getNotFoundValue() {
		return null;
	}
		
}
