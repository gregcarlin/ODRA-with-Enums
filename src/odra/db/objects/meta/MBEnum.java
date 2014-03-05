package odra.db.objects.meta;


import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.sbql.builder.BuilderUtils;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase objects
 * representing enums.
 * 
 * @author blejam
 */

public class MBEnum extends MBObject{

		/**
	 * Initializes the MBEnum object
	 * @param oid OID of an existing enum or an empty complex object
	 */
	public MBEnum(OID oid) throws DatabaseException{
		super(oid);
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}
	
	/**
	 * Initializes the enum in the metabase by creating some system-level subobjects.
	 * @param size size of a buffer used to store fields
	 */
	public void initialize(int typenameid, int fieldbuf) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.ENUM_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName(Names.namesstr[Names.TYPEID_ID]), oid, typenameid);
		store.createComplexObject(store.addName(Names.namesstr[Names.FIELDS_ID]), oid, fieldbuf);
		store.createComplexObject(store.addName(Names.namesstr[Names.VALUE_ID]), oid, fieldbuf);
		store.createBooleanObject(store.addName(Names.namesstr[Names.STATE]), oid, false);
	}
	
	/**
	 * return true if it is really a enum object?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.ENUM_OBJECT;
	}
	
	public OID createField(String name,byte[] astField) throws DatabaseException {
				
		return store.createBinaryObject(store.addName(name), getFieldsRef(), astField, 0);
	}
	
	public OID createFieldValue(String name, int mincard, int maxcard, String type, int ref) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		int typeid = metaBase.addMetaReference(type);

		OID varid = metaBase.createComplexObject(name, getFieldsRefValue(), MBVariable.FIELD_COUNT);
		new MBVariable(varid).initialize(typeid, mincard, maxcard, ref);
	
		return varid;
	}

	public OID[] getFields() throws DatabaseException {
		return getFieldsRef().derefComplex();
	}
	
	public OID[] getFieldsValue() throws DatabaseException {
		return getFieldsRefValue().derefComplex();
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
	 * @return type (valid only if the module has been linked)
	 */
	public OID getType() throws DatabaseException
	{
		DBModule module = getModule();

		if (ConfigDebug.ASSERTS)
			assert module.isModuleLinked() : "uncompiled module";

		int typeid = getTypeNameIdRef().derefInt();
		OID typeoid = getMetaBase().getCompiledMetaReferenceAt(typeid).derefReference();
		return typeoid;
	}
	
	public void setState(boolean state) throws DatabaseException{
		OID oid = getStateRef();
		oid.updateBooleanObject(state);
		
	}
	
	public boolean getState()throws DatabaseException{
		return getStateRef().derefBoolean();
	}
	
	/***********************************
	 * debugging
	 * */
	
	public String dump(String indend) throws DatabaseException {
		
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String metaenu = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + 
					" (" + mobjname + ") is #" + getTypeNameIdRef().derefInt() + " ("+ getTypeName()+ ") [enum]\n";

		OID[] enuchildren = getFieldsRef().derefComplex();
		for (int j = 0; j < enuchildren.length; j++)
			metaenu += "\t" + enuchildren[j].toString() + "\t\t" + indend + "  fld. " + j + " #" + enuchildren[j].getObjectNameId() + "("+enuchildren[j].getObjectName()+")"+ BuilderUtils.deserializeAST(enuchildren[j].derefBinary()).toString() +"\n";
		
		return metaenu;
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getTypeNameIdRef() throws DatabaseException {
		return oid.getChildAt(TYPE_NAME_ID_POS);
	}
	
	private final OID getFieldsRef() throws DatabaseException {
		return oid.getChildAt(FIELDS_POS);
	}
	
	private final OID getFieldsRefValue() throws DatabaseException {
		return oid.getChildAt(FIELDS_VALUE_POS);
	}
	
	private final OID getStateRef() throws DatabaseException {
		return oid.getChildAt(STATE_POS);
	}
	
	private final static int TYPE_NAME_ID_POS = 1;
	private final static int FIELDS_POS = 2;
	private final static int FIELDS_VALUE_POS = 3;
	private final static int STATE_POS = 4;
	
	
}
