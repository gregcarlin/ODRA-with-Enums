package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.recordtypes.MultiKeyRecordType;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.RecordTypeKind;
import odra.db.objects.data.DBModule;
import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.results.compiletime.StructSignature;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides an API for metabase objects
 * representing indices. Indices can be invoked
 * in a manner similar to procedures. That is the
 * reason why we keep metabase records about them.
 * <br>
 * MBIndex object structure:<br>
 * <ul>
 * <li>MBObject kind (INDEX_OBJECT)</li>
 * <li><b>varref</b> declaration of the variable being indexed (an MBVariable object)</li>
 * <li>nonkey AST (textual)</li>
 * <li>keys description
 * <ul>
 * <li>key#1 description
 * <ul>
 * <li>key AST (textual)</li>
 * <li>coresponding record type reference</li>
 * </ul></li>
 * <li>key#2 description</li>
 * <li>...</li>
 * </ul></li>
 * <li><b>uniqueNonkeys</b> - true if nonkey values are unique</li>
 * </ul>
 * @author raist, tkowals
 * @version 1.0
 */

public class MBIndex extends MBObject {	
	/**
	 * Initializes a new MBIndex object
	 * @param oid oid of an existing object
	 */
	public MBIndex(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the variable in the metabase by creating special system
	 * subobjects describing the index
	 * @param varref declaration of the variable being indexed (an MBVariable object) 
	 * @param temporary true for temporary, false for materialized index
	 * @param sign result signature for index creating query
	 * @param recordType record type associated with index
	 * @param uniqueNonkeys true if nonkey values are unique in index
	 * @throws DatabaseException
	 */
	public void initialize(OID varref, boolean temporary, StructSignature sign, RecordType recordType, boolean uniqueNonkeys) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.INDEX_OBJECT.kindAsInt());
		store.createReferenceObject(store.addName("$idxvar"), oid, varref);			
		try {
			store.createStringObject(store.addName("$nonkeyast"), oid, AST2TextQueryDumper.AST2Text(sign.getFields()[0].getOwnerExpression()), 0);
		} catch (Exception e) {
			throw new DatabaseException("Unable to convert index AST to text");
		}
		OID keysoid = store.createComplexObject(store.addName("$keysinfo"), oid, sign.getFields().length - 1);
		for (int i = 1; i < sign.getFields().length; i++) {
			OID keyoid = store.createComplexObject(store.addName("$keyinfo"), keysoid, KEYINFOFIELD_COUNT);
			try {
				Expression keyexpr = sign.getFields()[i].getOwnerExpression();
				if (keyexpr instanceof DerefExpression) 
					keyexpr = ((DerefExpression) keyexpr).getExpression();
				store.createStringObject(store.addName("$keyast"), keyoid, AST2TextQueryDumper.AST2Text(keyexpr), 0);	
			} catch (Exception e) {
				throw new DatabaseException("Unable to convert index AST to text");
			}
			RecordType curRT;
			if (recordType instanceof MultiKeyRecordType) curRT = ((MultiKeyRecordType) recordType).getRecordType(i - 1); 
			else curRT = recordType;
			store.createReferenceObject(store.addName("$recordtype"), keyoid, curRT.getOID());
		}
		store.createBooleanObject(store.addName("$uniquenonkeys"), oid, uniqueNonkeys);
	}

	/**
	 * @return true if the oid really represent a metaindex
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.INDEX_OBJECT;
	}

	/**
	 * Can only be used if the module has been linked.
	 * @return type of the index result.
	 */
	public OID getType() throws DatabaseException {
		DBModule module = getModule();

		if (ConfigDebug.ASSERTS) assert module.isModuleLinked() : "uncompiled module";
		
		return MBObjectFactory.getTypedMBObjectTypeOID(getIdxVar());
	}	
		
	
	/**
	 * @return physical location of the metabase object representing the indexed variable
	 */
	public OID getIdxVar() throws DatabaseException {
		return getIdxVarRef().derefReference();
	}

	/***********************************
	 * debugging
	 * */

	public String dump(String indend) throws DatabaseException {		
		int mobjnameid = oid.getObjectNameId();
		OID idxvarid = getIdxVar();
		String mobjname = oid.getObjectName();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		metastr += " on #" + idxvarid + " (" + idxvarid.toString() + ") [index]\n";

		return metastr;
	}

	/**
	 * @return textual AST of index nonkey
	 * @throws DatabaseException
	 */
	public String getNonKeyASTText() throws DatabaseException {
		return getNonKeyASTRef().derefString();				
	}

	private OID getNonKeyASTRef() throws DatabaseException {
		return oid.getChildAt(NONKEYAST_POS);
	}
	
	/**
	 * @param index number of key in index
	 * @return textual AST of index key
	 * @throws DatabaseException
	 */
	public String getKeyASTText(int index) throws DatabaseException {
		return getKeyASTRef(index).derefString();				
	}
	
	private OID getKeyASTRef(int index) throws DatabaseException {
		return getKeyInfoRef(index).getChildAt(KEYAST_POS);				
	}	
	
	/**
	 * @param index number of key in index
	 * @return record type object associated with index key
	 * @throws DatabaseException
	 */
	public RecordType getRecordType(int index) throws DatabaseException {
		return RecordTypeKind.generateRecordType(getRecordTypeRef(index));				
	}	
	
	/**
	 * @return number of index keys
	 * @throws DatabaseException
	 */
	public int countKeys() throws DatabaseException {
		return getKeysRef().countChildren();
	}
	
	/**
	 * @return true if nonkey values in index are unique
	 * @throws DatabaseException
	 */
	public boolean getAreNonkeysUnique() throws DatabaseException {
		return getUniqueNonkeysRef().derefBoolean();
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private OID getIdxVarRef() throws DatabaseException {
		return oid.getChildAt(VARREF_POS);
	}

	private OID getKeyInfoRef(int index) throws DatabaseException {
		return getKeysRef().derefComplex()[index];
	}
	
	private OID getKeysRef() throws DatabaseException {
		return oid.getChildAt(KEYSINFO_POS);
	}

	private OID getRecordTypeRef(int index) throws DatabaseException {
		return getKeyInfoRef(index).getChildAt(RECORDTYPE_POS).derefReference();				
	}

	private OID getUniqueNonkeysRef() throws DatabaseException {
		return oid.getChildAt(UNIQUENONKEYS_POS);
	}
	
	private final static int VARREF_POS = 1;
	private final static int NONKEYAST_POS = 2;
	private final static int KEYSINFO_POS = 3;
	private final static int UNIQUENONKEYS_POS = 4;
	
	public final static int FIELD_COUNT = 6;
	
	private final static int KEYAST_POS = 0;
	private final static int RECORDTYPE_POS = 1;
	
	private final static int KEYINFOFIELD_COUNT = 3;
}
