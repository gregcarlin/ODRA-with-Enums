package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations
 * on metabase objects representing type definitions.
 * 
 * @author raist
 */

public class MBTypeDef extends MBObject {
	/**
	 * Initializes a new MBTypeDef object using an existing complex object
	 * @param oid oid of an existing typedef object (or an empty complex object)
	 */
	public MBTypeDef(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the non-distinct typedef in the metabase
	 * @param typenameid position in the list of module's metareferences
	 */
	public void initialize(int typenameid) throws DatabaseException {
		initialize(typenameid, false);
	}
	
	/**
	 * Initializes the typedef in the metabase
	 * @param typenameid position in the list of module's metareferences
     * @param isDistinct indicates whether the typedef introduces a distinct type
	 */
	public void initialize(int typenameid, boolean isDistinct) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.TYPEDEF_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), oid, typenameid);
		store.createBooleanObject(store.addName("$distinct"), oid, isDistinct);
	}
	
	/**
	 * return true if it is really a typedef object?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.TYPEDEF_OBJECT;
	}
	
	/**
	 * @return position in the list of module's metareferences
	 */
	public int getTypeNameId() throws DatabaseException {
		return getTypeNameIdRef().derefInt();
	}
	
	/**
	 * @return name of the type
	 */
	public String getTypeName() throws DatabaseException {
		return getMetaBase().getMetaReferenceAt(getTypeNameId()).derefString();
	}	

	/**
	 * @return true if the typedef introduces a distinct type
	 */
	public boolean isDistinct() throws DatabaseException {
		return getDistinctRef().derefBoolean();
	}	

	/**
	 * @return base type of the typedef (valid only if the module has been linked)
	 */
	public OID getType() throws DatabaseException {
		DBModule module = getModule();
		
		if (ConfigDebug.ASSERTS) assert module.isModuleLinked() : "uncompiled module";

		int typeid = getTypeNameIdRef().derefInt();
		
		return getMetaBase().getCompiledMetaReferenceAt(typeid).derefReference();
	}

	
	/* (non-Javadoc)
	 * @see odra.db.objects.meta.MBObject#getSubMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
		return MBObjectFactory.getTypedMBObject(getType()).getNestedMetabaseEntries();
	}

	/***********************************
	 * debugging
	 * */

	public String dump(String indend) throws DatabaseException {
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();
		String distinct = isDistinct() ? "distinct " : "";
		
		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid 
			+ " (" + mobjname + ") is #" + getTypeNameIdRef().derefInt() + " (" 
			+ getTypeName()
			+ ") [" +  distinct + "typedef]\n";

		return metastr;
	}

	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getTypeNameIdRef() throws DatabaseException {
		return oid.getChildAt(TYPE_NAME_ID_POS);
	}

	private final OID getDistinctRef() throws DatabaseException {
		return oid.getChildAt(DISTINCT_POS);
	}

	private final static int TYPE_NAME_ID_POS = 1;
	private final static int DISTINCT_POS = 2;
	
	public final static int FIELD_COUNT = 3; 
}
