package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for metabase objects
 * representing primitive types.
 * 
 * @author raist
 */

public class MBPrimitiveType extends MBObject {

	/**
	 * Initializes a new MBPrimitiveType object.
	 * @param oid OID of an existing primitive type object 
	 */
	public MBPrimitiveType(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the database object creating some system-level subobjects
	 * @param name of a primitive type
	 */
	public void initialize(String typename) throws DatabaseException {
		PrimitiveTypeKind type = PrimitiveTypeKind.getForExternalName(typename);

		
		assert type != null : "unknown primitive type name";

		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.PRIMITIVE_TYPE_OBJECT.kindAsInt());
		store.createIntegerObject(store.addName("$type"), oid, type.kindAsInt());	
	}

	/**
	 * Is this object really a primitive type declaration?
	 */
	public final boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.PRIMITIVE_TYPE_OBJECT;
	}

	/**
	 * Returns an object indicating what primitive type the MBPrimitiveType objects represents 
	 * @return type of the MBPrimitiveType
	 */
	public PrimitiveTypeKind getTypeKind() throws DatabaseException {		
		return PrimitiveTypeKind.getForInteger(getTypeRef().derefInt());	
	}

	/***********************************
	 * debugging
	 * */
	
	public String dump(String indend) throws DatabaseException {
			
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();
		
		return "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ") [primitive type]\n";
	}
	
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */
	
	private final OID getTypeRef() throws DatabaseException {
		return oid.getChildAt(TYPE_POS);
	}
	
	private final static int TYPE_POS = 1;
}
