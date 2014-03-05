package odra.db.indices;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.dataaccess.DBObjectDirectKeyAccess;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.OIDAccess;
import odra.db.indices.keytypes.ASTTextKeyType;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.SimpleRecordType;
import odra.db.indices.structures.IndexStructureKind;
import odra.db.indices.structures.LinearHashingMap;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBIndex;
import odra.system.Names;


/**
 * 
 * NonkeyIndexRegister class manages information concerning indices set on
 * unique nonkey (must have unique nonkey AST and objects varref).
 * <br>
 * Information are stored inside Index Register in "uniquenonkey" objects.
 * <br>
 * Individual Nonkey Index Register structure :
 * <ul>
 * <li>unkast (query text of unique nonkey value)</li>
 * <li>unkvarref (varref of unique nonkey value object)</li>
 * <li> unkindexlist (list of indices with unique nonkey value AST and objects varref)
 * <ul>
 * <li>MBIndex#1 oid (name of this reference object is "module.indexname");</li>
 * <li>MBIndex#2 oid</li>
 * <li>.....</li>
 * </ul></li>
 * <li>keylist (list of unique key values AST occuring in indices from unkindexlist)
 * <ul>
 * <li>uniquekey#1 (list of indices containing unique key value AST)
 * <ul>
 * <li>keyast (unique key value query text)
 * <li>keyinfo#1 (specify location of single key value)
 * <ul>
 * <li>unkidxnr (number of index in unkindexlist)</li>
 * <li>keynr (key number in this index)</li>
 * </ul></li>
 * <li>keyinfo#2
 * <li>.....</li>
 * </ul>
 * </li>
 * <li>uniquekey#2
 * <li>.....</li>
 * </li>
 * </ul></li>
 * <li>keyindex (index of uniquekeys indexed by AST)</li> 
 * </ul>
 * @author tkowals
 * 
 */
public class NonkeyIndexRegister {

	private OID oid;
	private DBModule admod;	

	/**
	 * @param oid nonkey index register complex object 
	 * @param admod admin module
	 */
	public NonkeyIndexRegister(OID oid, DBModule admod) {
		this.oid = oid;
		this.admod = admod;
	}

	/**
	 * Initializes the nonkey-index register in the database by creating some system-level subobjects.
	 * @param mbidx MBIndex object describing first added index current nonkey index register 
	 * @throws DatabaseException
	 */
	public void initialize(MBIndex mbidx) throws DatabaseException {

		admod.createStringObject("$unkast", oid, mbidx.getNonKeyASTText(), 0);
		admod.createReferenceObject("$unkvarref", oid, mbidx.getIdxVar());
		admod.createComplexObject("$unkindexlist", oid, 0);
		admod.createComplexObject("$keylist", oid, 0);
		OID keyidxoid = admod.createComplexObject("$keyindex", oid, 0);
		
		RecordType recordType = new SimpleRecordType(new ASTTextKeyType());
		recordType.initialize(admod.createComplexObject(Names.namesstr[Names.RECORDTYPE_ID], keyidxoid, 1));
		DataAccess dataAccess = new DBObjectDirectKeyAccess();
		dataAccess.initialize(admod.createComplexObject(Names.namesstr[Names.DATAACCESS_ID], keyidxoid, 1)); 		
		
		// TODO: change to some less space consuming index (simple sorted list)
		LinearHashingMap nidxmap = (LinearHashingMap) IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID, admod.createComplexObject("$index", keyidxoid, LinearHashingMap.FIELDS_COUNT), recordType, dataAccess); 
		nidxmap.initialize(13, 3, 75, 65);
		
	}
	
	void registerKeys(OID mbidxoid) throws DatabaseException {

		MBIndex mbidx = new MBIndex(mbidxoid);
		
		int idxnum = countUNKIndices(oid);
		admod.createReferenceObject(mbidx.getName(), getUNKIndexListRef(oid), mbidxoid);

		for(int i = 0; i < mbidx.countKeys(); i++) {
		
			Index keyidx = getKeyIndex(oid);
			OID ukeyoid = (OID) keyidx.lookupItem(mbidx.getKeyASTText(i));
			if (ukeyoid == IndexManager.ASTNOTFOUND) {
				ukeyoid = admod.createComplexObject("$uniquekey", getUNKKeyListRef(oid), 0);
				admod.createStringObject("$keyast", ukeyoid, mbidx.getKeyASTText(i), 0);
				keyidx.insertItem(mbidx.getKeyASTText(i), ukeyoid);
			}
			OID keyinfooid = admod.createComplexObject("$keyinfo", ukeyoid, 2);
			admod.createIntegerObject("$unkidxnr", keyinfooid, idxnum);
			admod.createIntegerObject("$keynr", keyinfooid, i);
			
		}
		
	}
	
	void unregisterKey(OID mbidxoid) throws DatabaseException {
		MBIndex mbidx = new MBIndex(mbidxoid);

		int unkidxnum = 0;
		while (!getUNKMBIdxRef(oid, unkidxnum).equals(mbidxoid)) unkidxnum++;
		
		for(int i = 0; i < mbidx.countKeys(); i++) {			
			Index keyidx = getKeyIndex(oid);
			OID ukeyoid = (OID) keyidx.lookupItem(mbidx.getKeyASTText(i));
			if (countUniqueKeyList(ukeyoid) == 1) {
				
				if (!keyidx.removeItem(mbidx.getKeyASTText(i), ukeyoid))
					throw new DatabaseException("Index of unique keys is damaged on " + mbidx.getKeyASTText(i));

				ukeyoid.delete();
			}
		}
		
		for(int i = 0; i < countUniqueKeys(oid); i++) {
			OID ukeyoid = getUniqueKeyRef(oid, i);

			int idxnum = 0;			
			while (idxnum < countUniqueKeyList(ukeyoid)) {
				
				if (getIndexNr(ukeyoid, idxnum) == unkidxnum)
					getUniqueKeyInfoRef(ukeyoid, idxnum).delete();
				else {
					if (getIndexNr(ukeyoid, idxnum) > unkidxnum)
						decreaseIndexNr(ukeyoid, idxnum);
					idxnum++;
				}					
			}			
		}		
	}
	
	/***********************************
	 * access to information needed by uniquenonkey and uniquekey indices 
	 * */
	
	/**
	 * @param ukeyoid unique key structure oid
	 * @return textual ast of unique key 
	 * @throws DatabaseException
	 */
	public static final String getUniqueKeyAst(OID ukeyoid) throws DatabaseException {
		return getUniqueKeyAstRef(ukeyoid).derefString();
	}
	
	/**
	 * @param unkoid unique nonkey structure oid
	 * @return textual ast of unique nonkey
	 * @throws DatabaseException
	 */
	public static final String getUNKAst(OID unkoid) throws DatabaseException {
		return getUNKAstRef(unkoid).derefString();
	}
	
	/**
	 * @param unkoid unique nonkey structure oid
	 * @return OID to MBObject describing indexed variable
	 * @throws DatabaseException
	 */
	public static final OID getUNKVar(OID unkoid)  throws DatabaseException {
		return getUNKVarRef(unkoid).derefReference();
	}

	/***********************************
	 * access to information needed by optimizer 
	 * */
	
	/**
	 * @param ukeyoid unique key structure oid
	 * @param idxnum number of key info
	 * @return index number for key info given by unique key and number
	 * @throws DatabaseException
	 */
	public static final int getIndexNr(OID ukeyoid, int idxnum) throws DatabaseException {
		return getUniqueKeyInfoRef(ukeyoid, idxnum).getChildAt(INDEXNR_POS).derefInt();
	}
	
	/**
	 * @param ukeyoid unique key structure oid
	 * @param idxnum number of key info
	 * @return index key number for key info given by unique key and number
	 * @throws DatabaseException
	 */
	public static final int getKeyNr(OID ukeyoid, int idxnum) throws DatabaseException {
		return getUniqueKeyInfoRef(ukeyoid, idxnum).getChildAt(KEYNR_POS).derefInt();
	}
	
	/**
	 * @param ukeyoid unique key structure oid
	 * @return number of indices using giver key and nonkey
	 * @throws DatabaseException
	 */
	public static final int countUniqueKeyList(OID ukeyoid) throws DatabaseException {
		return ukeyoid.countChildren() - 1;
	}	
	
	
	/***********************************
	 * access to subobjects describing the UniqueKey for given UniqueNonkey
	 * */
	
	private static final OID getUniqueKeyAstRef(OID ukeyoid) throws DatabaseException {
		return ukeyoid.getChildAt(KEYAST_POS);
	}
	
	private static final OID getUNKAstRef(OID unkoid)  throws DatabaseException {
		return unkoid.getChildAt(UNKAST_POS);
	}
	
	private static final OID getUNKVarRef(OID unkoid)  throws DatabaseException {
		return unkoid.getChildAt(UNKVARREF_POS);
	}	
	
	private static final void decreaseIndexNr(OID ukeyoid, int idxnum) throws DatabaseException {
		getUniqueKeyInfoRef(ukeyoid, idxnum).getChildAt(INDEXNR_POS).updateIntegerObject(getIndexNr(ukeyoid, idxnum) - 1);
	}
	
	private static final OID getUniqueKeyInfoRef(OID ukeyoid, int idxnum) throws DatabaseException {
		return ukeyoid.getChildAt(idxnum + 1);
	}
	
	private static final OID getUniqueKeyRef(OID unkoid, int keynum) throws DatabaseException {
		return getUNKKeyListRef(unkoid).getChildAt(keynum);
	}	
	
	/***********************************
	 * access to subobjects describing the UniqueNonkey
	 * */ 

	/**
	 * @param unkoid unique nonkey structure oid
	 * @return indices created on given nonkey
	 * @throws DatabaseException
	 */
	public static final int countUNKIndices(OID unkoid) throws DatabaseException {
		return getUNKIndexListRef(unkoid).countChildren();
	}
	
	/**
	 * @param unkoid unique nonkey structure oid
	 * @param idxnum number of index stored in current nonkey index
	 * @return MBIndex for index specified by parameters
	 * @throws DatabaseException
	 */
	public static final MBIndex getUNKMBIndex(OID unkoid, int idxnum) throws DatabaseException {
		return new MBIndex(getUNKIndexListRef(unkoid).getChildAt(idxnum).derefReference());
	}
	
	private static final OID getUNKMBIdxRef(OID unkoid, int idxnum) throws DatabaseException {
		return getUNKIndexListRef(unkoid).getChildAt(idxnum).derefReference();
	}
	
	private static final int countUniqueKeys(OID unkoid) throws DatabaseException {
		return getUNKKeyListRef(unkoid).countChildren();
	}
	
	/**
	 * @param unkoid unique nonkey structure oid
	 * @return index of indices indexed by their keys ASTs
	 * @throws DatabaseException
	 */
	public static final Index getKeyIndex(OID unkoid) throws DatabaseException {
		RecordType recordType = new SimpleRecordType(getUNKRecordTypeRef(unkoid));
		DataAccess dataAccess = new OIDAccess(getUNKDataAccessRef(unkoid));
		return IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID, getUNKKeyIndexRef(unkoid).getChildAt(INDEXOID_POS), recordType, dataAccess);
	}
	
	/***********************************
	 * access helpers to subobjects describing the IndexManager state
	 * */ 
	
	private static final OID getUNKIndexListRef(OID unkoid) throws DatabaseException {
		return unkoid.getChildAt(UNKINDEXLIST_POS);
	}	
	
	private static final OID getUNKKeyListRef(OID unkoid) throws DatabaseException {
		return unkoid.getChildAt(KEYLIST_POS);
	}
	
	private static final OID getUNKKeyIndexRef(OID unkoid) throws DatabaseException {
		return unkoid.getChildAt(KEYINDEX_POS);
	}

	private static final OID getUNKRecordTypeRef(OID unkoid) throws DatabaseException {
		return getUNKKeyIndexRef(unkoid).getChildAt(RECORDTYPE_POS);
	}
	
	private static final OID getUNKDataAccessRef(OID unkoid) throws DatabaseException {
		return getUNKKeyIndexRef(unkoid).getChildAt(DATAACCESS_POS);
	}	

	// nonkeylist subfields are uniquenonkey
	// nonkeylist subfields	
	private static final int UNKAST_POS = 0;
	private static final int UNKVARREF_POS = 1;
	private static final int UNKINDEXLIST_POS = 2;
	private static final int KEYLIST_POS = 3;	
	private static final int KEYINDEX_POS = 4;
	
	// keylist subfields are uniquekey fields
	// uniquekey subfields are 
	private static final int KEYAST_POS = 0;
	// and keyinfo fields
	// keyinfo subfields are
	private static final int INDEXNR_POS = 0;
	private static final int KEYNR_POS = 1;
	
	// keyindex subfields
	private final static int RECORDTYPE_POS = 0;
	private final static int DATAACCESS_POS = 1;
	private final static int INDEXOID_POS = 2;
	
}
