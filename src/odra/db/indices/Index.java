package odra.db.indices;

import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.dataaccess.*;
import odra.db.indices.recordtypes.*;
import odra.db.indices.updating.IndexRecordLocation;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.ReferenceResult;
import odra.sbql.results.runtime.StructResult;
import odra.system.Names;

/**
 * This class is a super-class for description of all types of index 
 * data structures 
 * The goal is to make data indexing technique indepentent of indexed data
 * <br>
 * Index structure:<br>
 * <ul>
 * <li>
 * general (properties of index)
 * <ul>
 * <li>counter of records stored in index</li>
 * </ul></li>
 * <li>
 * properties (current properties of index)
 * <ul>
 * <li>...</li>
 * </ul></li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */
public abstract class Index {

	protected OID oid;

	protected OID general_oid;
	
	protected OID properties_oid;
	
	protected OID index_oid;
	
	protected IDataStore store;
	
	/**
	 * Record type associated with index
	 */
	public RecordType recordType;
	
	/**
	 * Dataacces type associated with index
	 */
	public DataAccess dataAccess;
	
	protected int recordCount;

	/**
	 * Creates a new LinearHashingMap object using a reference
	 * to an existing LinearHashingMap object (or an empty complex object).
	 * @param oid complex object with or for index description
	 * @param recordType_id type of records stored in index
	 * @param dataAccess_id specifies access to data stored in index
	 * @param keyParam describes key value for data access
	 */	
	public Index(OID oid, RecordType recordType, DataAccess dataAccess) throws DatabaseException {

		this.oid = oid;
		this.store = oid.getStore();

		this.recordType = recordType;
		this.dataAccess = dataAccess;
		
		if (oid.countChildren() >= FIELDS_COUNT) {		
			general_oid = oid.getChildAt(GENERAL_POS);
			properties_oid = oid.getChildAt(PROPERTIES_POS);			
			index_oid = oid.getChildAt(INDEX_POS);
			this.recordCount = getRecordCount();
		}
	}
	
	/**
	 * Initializes the index in the database by creating some system-level subobjects.
	 * @throws DatabaseException
	 */
	public void initialize() throws DatabaseException {
		this.general_oid = store.createComplexObject(Names.GENERAL_ID, oid, 2);
		store.createIntegerObject(Names.RECORDCOUNT_ID, general_oid, 0);

	}
	
	/**
	 * @param bres result obtained from creating query
	 * @throws DatabaseException
	 */
	public void insertResult(BagResult bres) throws DatabaseException {
		
		StructResult stres;
		ReferenceResult r0res;
		
		if (recordType instanceof MultiKeyRecordType) {
			for(int i = 0; i < bres.elementsCount(); i++) {
				stres = (StructResult) bres.elementAt(i);
				r0res = (ReferenceResult) stres.fieldAt(0);				
			
				stres.removeField(r0res);
				insertItem(stres, r0res);
			}
		} else {
			for(int i = 0; i < bres.elementsCount(); i++) {
				stres = (StructResult) bres.elementAt(i);						
				r0res = (ReferenceResult) stres.fieldAt(0);				
				
				insertItem(stres.fieldAt(1), r0res);
			}
		}
		
	}

	/**
	 * @param bres result obtained from creating query
	 * @throws DatabaseException
	 */
	public void insertTemporaryResult(BagResult bres) throws DatabaseException {
	
		if (recordType instanceof MultiKeyRecordType) {
			StructResult stres, keyres;			
			for(int i = 0; i < bres.elementsCount(); i++) {
				stres = (StructResult) bres.elementAt(i);				
				keyres = new StructResult();
				for (int j = 1; j < stres.fieldsCount(); j++)
					keyres.addField(stres.fieldAt(j));
				this.recordType.adjust2KeyValue(dataAccess.key2keyValue(keyres));
			}
		} else {
			for(int i = 0; i < bres.elementsCount(); i++)  	
				this.recordType.adjust2KeyValue(dataAccess.key2keyValue(((StructResult) bres.elementAt(i)).fieldAt(1)));
		}
		
		if (recordType instanceof MultiKeyRecordType) {
			StructResult stres, keyres;			
			for(int i = 0; i < bres.elementsCount(); i++) {
				stres = (StructResult) bres.elementAt(i);				
				keyres = new StructResult();
				for (int j = 1; j < stres.fieldsCount(); j++)
					keyres.addField(stres.fieldAt(j));
				insertItem(keyres, i);
			}
		} else {
			for(int i = 0; i < bres.elementsCount(); i++) 	
				insertItem(((StructResult) bres.elementAt(i)).fieldAt(1), i);
		}
		
	}	
	
	/**
	 * @param keyValue search key value
	 * @return true if key value is already stored in index
	 * @throws DatabaseException
	 */
	public boolean containsKey(Object keyValue) throws DatabaseException {
		return !lookupItem(keyValue).equals(dataAccess.getNotFoundValue()); 
	}

	/**
	 * Used if keys are unique.
	 * @param keyValue search key value
	 * @return nonkey value matching given key
	 * @throws DatabaseException
	 */
	public abstract Object lookupItem(Object keyValue) throws DatabaseException;

	/**
	 * @param keyValue search key value
	 * @return array of nonkey values matching given key
	 * @throws DatabaseException
	 */
	public abstract Object[] lookupItemsEqualTo(Object keyValue) throws DatabaseException;
	
	/**
	 * @param keyValue search key value criteria (as set, range of keyvalues)
	 * @return array of nonkey values matching given key criteria
	 * @throws DatabaseException
	 */
	public abstract Object[] lookupItemsInRange(Object keyValue) throws DatabaseException;
	
	/**
	 * Inserts nonkey value to an index according to key value.
	 * @param key holds key value
	 * @param nonkey nonkey value
	 * @throws DatabaseException
	 */
	public abstract void insertItem(Object key, Object nonkey) throws DatabaseException;
	
	/**
	 * Removes nonkey value from an index.
	 * @param key holds key value
	 * @param nonkey nonkey value
	 * @return true if nonkey was found and successfully removed
	 * @throws DatabaseException
	 */
	public abstract boolean removeItem(Object key, Object nonkey) throws DatabaseException;

	public abstract IndexRecordLocation getItemLocation(Object key, Object nonkey) throws DatabaseException;
		
	/**
	 * Moves nonkey value within an index.
	 * @param beforekeyvalue key value before updating
	 * @param lookupNonkeyArray TEMPORARELY used for automatic index updating <- should not be used (maybe two phase move method 1) find old, move to new).
	 * @param newkey holds current key value
	 * @param nonkey nonkey value
	 * @return true if nonkey was found and successfully moved
	 * @throws DatabaseException
	 */
	public abstract boolean moveItem(IndexRecordLocation beforeRecord, Object newkey, Object nonkey) throws DatabaseException;

	/**
	 * Checks if new key value can be safely added for indexing.
	 * Otherwise performs all neccesary index adjustments.
	 * @param newkey holds key value
	 * @return true if adjustment was performed
	 * @throws DatabaseException 
	 */
	public abstract boolean adjustKey(Object newkey) throws DatabaseException;
	
	/** 
	 * @return number of records in hash map 
	 */
	public int getRecordCount() throws DatabaseException {
		return getRecordCountRef().derefInt();
	}	
	
	/**
	 * @param recordCount MOE TYLKO INC I DEC
	 */
	protected void setRecordCount(int recordCount) throws DatabaseException {
		this.recordCount = recordCount;
		getRecordCountRef().updateIntegerObject(recordCount);
	}

	private final OID getRecordCountRef() throws DatabaseException {
		return general_oid.getChildAt(COUNT_POS);
	}
	
	protected static final int GENERAL_POS = 0;
	protected static final int PROPERTIES_POS = 1;
	protected static final int INDEX_POS = 2;

	private final static int COUNT_POS = 0;

	
	public final static int FIELDS_COUNT = 3;
	
}
