package odra.db.indices.structures;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.recordtypes.RecordType;

/**
 * This class contains only static public members.
 * Is used as an "enumeration" to describe
 * kinds of structures used for indexing  
 * 
 * @author tkowals
 * @version 1.0
 */

public class IndexStructureKind {

	private IndexStructureKind() {
		
	}
	
	/**
	 * Generates Index object serialized in database store. 
	 * @param kind id of index to generate according to "enumeration"
	 * @param oid address index structure in database store
	 * @param recordType record type associated with index
	 * @param dataAccess dataacces type associated with index
	 * @return index object
	 * @throws DatabaseException
	 */
	public static Index generateIndex(int kind, OID oid, RecordType recordType, DataAccess dataAccess) throws DatabaseException {
		
		recordType.setDataAccess(dataAccess);
		dataAccess.setRecordType(recordType);

		switch (kind) {
		case LINEARHASHINGMAP_ID: return new LinearHashingMap(oid, recordType, dataAccess); 
		}
		
		return null;
	}
	
	/**
	 * IDs of different index structures
	 */
	public final static int LINEARHASHINGMAP_ID = 1;
}
