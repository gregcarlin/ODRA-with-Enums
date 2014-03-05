package odra.db.indices.dataaccess;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.sbastore.NameIndex;
import odra.system.Sizes;

/**
 * This class provides access to indexing NameIndex.
 * Should be used only with StringRecordType.<br>  
 * Single index record contain integer number 
 * associated with some string in database NameIndex.   
 * <br><br>
 * NameIndexAccess complex object structure:<ul>
 * <li> NAMEINDEXACCESS_ID </li>
 * </ul> 
 * @author tkowals
 * @version 1.0
 */
public class NameIndexAccess extends DataAccess {
	
	/**
	 * Creates NameIndexAccess 
	 * serialized in complex object given by oid parameter.
	 * @param oid complex object containing dataaccess description
	 */
	public NameIndexAccess(OID oid) {
		super(oid);
	}
	
	/**
	 * Creates uninitialized NameIndexAccess. 
	 */
	public NameIndexAccess() {

	}
	
	@Override
	public Object key2keyValue(Object key) {
		return key;
	}
	
	@Override
	public Object record2nonkey(byte[] record) {
		return ByteBuffer.wrap(record).getInt();
	}

	@Override
	public Object nonkey2key(Object nonkey) throws DatabaseException {
		assert false:"unimlemented unused?";
		return store.getName((Integer) nonkey);
	}

	@Override
	public byte[] prepareRecordArray(Object key, Object nonkey) {
		return ByteBuffer.allocate(getNonkeyRecordSize()).putInt((Integer) nonkey).array();
	}
	
	@Override
	public Object record2keyValue(byte[] record) throws DatabaseException {
		return store.getName(ByteBuffer.wrap(record).getInt());
	}
	
	@Override
	protected int getKindID() {
		return DataAccessKind.NAMEINDEXACCESS_ID;
	}
	
	/**
	 * @return 4 - in case of NameIndexAccess individual record stored in index consist of one int value 
	 */
	public int getNonkeyRecordSize() {
		return Sizes.INTVAL_LEN;
	}
	
	/**
	 * @return NameIndex.NAMENOTFOUND - default value returned by index if name has not been found
	 */
	public Object getNotFoundValue() {
		return NameIndex.NAMENOTFOUND;
	}
	
}
