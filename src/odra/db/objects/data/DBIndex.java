package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.Index;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.DataAccessKind;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.RecordTypeKind;
import odra.db.indices.structures.IndexStructureKind;
import odra.db.indices.structures.LinearHashingMap;
import odra.db.indices.updating.TriggersManager;
import odra.sbql.interpreter.SBQLInterpreter;
import odra.sbql.results.runtime.BagResult;
import odra.sessions.Session;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides functionality of index objects stored
 * in the database.<br><br>
 *
 * Index structure:<br>
 * <ul>
 * <li>kind (object kind)</li>
 * <li>temporary (true for temporary, false for materialized index)</li>
 * <li>indextype (specifies type of index used)</li>
 * <li>recordtype (specifies types of records stored in an index)</li>
 * <li>dataaccess (specifies the access to data)</li>
 * <li>creating query (describing indexed data)<ul>
 * <li>query string</li>
 * <li>serialized bytecode generating objects and key values</li>
 * <li>constant pool for bytecode</li>
 * </ul></li> 
 * <li>indexBody (contains indexing structure)</li>
 * <li>automaticindexupdatingmodule (contains module of information required for automatic index updating)</li>
 * </ul>
 * @author tkowals
 * @version 1.0
 */

public class DBIndex extends DBObject {
	
	public DBIndex(OID idxptr) throws DatabaseException {
		super(idxptr);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	public void initialize(int indexStruct_id, boolean temporary, RecordType recordType, DataAccess dataAccess, String query, byte[] bytecode, byte[] cnstpool) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.INDEX_OBJECT);
		store.createBooleanObject(store.addName("$tempflag"), oid, temporary);
		store.createIntegerObject(store.addName("$idxstruct"), oid, indexStruct_id);
		store.createIntegerObject(Names.INDEXTYPE_ID, oid, recordType.getRecordTypeID());
		recordType.initialize(store.createComplexObject(store.addName("$recordtypes"), oid, 1));
		dataAccess.initialize(store.createComplexObject(store.addName("$dataaccess"), oid, 1));
		OID cqueryoid = store.createComplexObject(store.addName("$cquery"), oid, 0);
		store.createStringObject(store.addName("$query"), cqueryoid, query, 0);
		store.createBinaryObject(Names.BYTECODE_ID, cqueryoid, bytecode, 0);
		store.createBinaryObject(Names.CNSTPOOL_ID, cqueryoid, cnstpool, 0);
		store.createComplexObject(store.addName("$idxbody"), oid, 0);
		store.createComplexObject(store.addName("$autoupdate"), oid, 0);		
	}

	/**
	 * @return true if object's oid represents a valid index
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.INDEX_OBJECT;
	}
	
	public int getIndexStructKind() throws DatabaseException {
		return getIndexStructRef().derefInt();
	}
	
	public String getCreatingQuery() throws DatabaseException {
		return getCreatingQueryRef().derefString();
	}
	
	private final RecordType getRecordType() throws DatabaseException {
		return RecordTypeKind.generateRecordType(getRecordTypeRef(), getIndexType());
	}
	
	private final DataAccess getDataAccess() throws DatabaseException {
		return DataAccessKind.generateDataAccess(getDataAccessRef(), new DBModule(this.oid.getParent().getParent()));
	}
	
	public final Index getIndex() throws DatabaseException {
		
		OID idxoid;
		 
		if (isTemporary()) {
			idxoid = Session.getTemporaryIndex(oid);
			if (idxoid == null)
				idxoid = initTemporaryIndex();
		} else 
			idxoid = getIdxBodyPtr();
		
		return IndexStructureKind.generateIndex(getIndexStructKind(), idxoid, getRecordType(), getDataAccess());
	}		
	
	/**
	 * @throws DatabaseException
	 */
	public OID initTemporaryIndex() throws DatabaseException {
		
		SBQLInterpreter interpreter = new SBQLInterpreter(new DBModule(this.oid.getParent().getParent()));
		interpreter.runCode(getByteCode(), getConstantPool());
		
		BagResult bres = new BagResult();
		bres.addAll(interpreter.getResult());
		
		OID idxoid = Session.addTemporaryIndex(oid, bres);
		
		LinearHashingMap lhm = (LinearHashingMap) getIndex();
		lhm.initialize(LHBUCKETSCOUNT, LHBUCKETCAPACITY, LHPERSPLITLOAD, LHPERMERGELOAD);
			
		lhm.insertTemporaryResult(bres);
		
		return idxoid;
	}
	
	public void removeTemporaryIndex() throws DatabaseException {
		assert isTemporary() : "Removing temporary index can be performed only on temporary index";

		Session.removeTemporaryIndex(oid);
	}
	
	private final int getIndexType() throws DatabaseException {
		return getIndexTypeRef().derefInt();
	}
	
	public final TriggersManager getTriggersManager(DBModule mod) throws Exception {
		return new TriggersManager(getTriggersManagerRef(), mod);
	}
	
	private final byte[] getByteCode() throws DatabaseException {
		return getByteCodeRef().derefBinary();
	}
	
	private final byte[] getConstantPool() throws DatabaseException {
		return getConstantPoolRef().derefBinary();
	}
	
	public final boolean isTemporary() throws DatabaseException {
		return getTemporaryFlagRef().derefBoolean();
	}
	
	/***********************************
	 * access to sub
	 * objects describing the index
	 * */
	
/*	private final OID getQueryRef() throws DatabaseException {
		return getCreatingQueryRef().getChildAt(QUERY_POS);
	}*/
	
	private final OID getByteCodeRef() throws DatabaseException {
		return getCreatingQueryRef().getChildAt(BYTECODE_POS);
	}
	
	private final OID getConstantPoolRef() throws DatabaseException {
		return getCreatingQueryRef().getChildAt(CNSTPOOL_POS);
	}
	
	private final OID getTemporaryFlagRef() throws DatabaseException {
		return oid.getChildAt(TEMPFLAG_POS);
	}
	
	private final OID getIndexStructRef() throws DatabaseException {
		return oid.getChildAt(INDEXSTRUCT_POS);
	}
	
	private final OID getIndexTypeRef() throws DatabaseException {
		return oid.getChildAt(INDEXTYPE_POS);
	}
	
	public final OID getRecordTypeRef() throws DatabaseException {
		return oid.getChildAt(RECORDTYPE_POS);
	}
	
	public final OID getDataAccessRef() throws DatabaseException {
		return oid.getChildAt(DATAACCESS_POS);
	}

	private final OID getCreatingQueryRef() throws DatabaseException {
		return oid.getChildAt(CREATINGQUERY_POS);
	}
	
	private final OID getIdxBodyPtr() throws DatabaseException {
		return oid.getChildAt(IDXBODY_POS);
	}		
	
	public final OID getTriggersManagerRef() throws DatabaseException {
		return oid.getChildAt(TRIGMANAGER_POS);
	}
	
	private static final int TEMPFLAG_POS = 1;
	private static final int INDEXSTRUCT_POS = 2;
	private static final int INDEXTYPE_POS = 3;
	private static final int RECORDTYPE_POS = 4;
	private static final int DATAACCESS_POS = 5;
	private static final int CREATINGQUERY_POS = 6;
	private final static int IDXBODY_POS = 7;
	private final static int TRIGMANAGER_POS = 8;
	
	final static int FIELD_COUNT = 9;

//	private static final int QUERY_POS = 0;
	private static final int BYTECODE_POS = 1;
	private static final int CNSTPOOL_POS = 2;
	
	
	
	public static final int LHBUCKETSCOUNT = 13,
		LHBUCKETCAPACITY = 5, 
		LHPERSPLITLOAD = 75, 
		LHPERMERGELOAD = 65;
	
}
