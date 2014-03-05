package odra.db.objects.data;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.meta.MBProcedure;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class delivers operations that can be performed
 * on database objects representing classes.
 * 
 * @author raist, radamus
 */

public class DBClass extends DBObject {
	public DBClass(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : oid.getObjectName();
	}
	
	
	/**
	 * Initializes the class in the database by creating some system-level subobjects.
	 */
	public void initialize(int instancenameid) throws DatabaseException {		
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.CLASS_OBJECT);
		store.createComplexObject(store.addName(Names.namesstr[Names.METHODS_ID]), oid, 0);
		store.createComplexObject(store.addName(Names.namesstr[Names.SUPERCLASSES_ID]), oid, 0);
		store.createIntegerObject(store.addName(Names.namesstr[Names.INSTANCE_NAME_ID]), oid, instancenameid);
	}
	
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.CLASS_OBJECT;
	}
	/**
	 * Creates a new runtime procedure and connects it to the view.
	 * @param name name of the procedure
	 * @param objBody intermediate code of the procedure (unused)
	 * @param binBody binary code of the procedure
	 * @param constants constant pool
	 */	
	public OID createMethod(String name,byte[] objBody, byte[] binBody, byte[] constants, byte[] catches) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert name != null && binBody != null;

		OID procid = store.createComplexObject(store.addName(name), getMethodsRef(), MBProcedure.FIELD_COUNT);

		DBProcedure prc = new DBProcedure(procid);
		prc.initialize(objBody, binBody, constants, catches);

		return procid;
	}
	
	public void addSuperClass(OID superid) throws DatabaseException{
		store.createPointerObject(store.addName("$super"), this.getSuperRef(), superid);
	}
	public OID getMethodsEntry() throws DatabaseException{
		return this.getMethodsRef();
	}
	
	public OID[] getSuperClassesRefs() throws DatabaseException{
		return this.getSuperRef().derefComplex();
	}
	
	public boolean hasInstanceName() throws DatabaseException{
		return this.getInstanceNameRef().derefInt() != NO_NAME;
	}
	public int getInstanceNameId() throws DatabaseException{
		return this.getInstanceNameRef().derefInt();
	}
	public boolean isSubClassOf(int classNameId) throws DatabaseException{
		for(OID sclsref: this.getSuperClassesRefs()){
			OID sclsid = sclsref.derefReference();
			if(sclsid.getObjectNameId() == classNameId)
				return true;
			if(new DBClass(sclsid).isSubClassOf(classNameId))
				return true;
		}
		return false;
	}
	
	public boolean isSubClassOf(OID classId) throws DatabaseException{
		for(OID sclsref: this.getSuperClassesRefs()){
			OID sclsid = sclsref.derefReference();
			if(sclsid.equals(classId))
				return true;
			if(new DBClass(sclsid).isSubClassOf(classId))
				return true;
		}
		return false;
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */
		
	private final OID getMethodsRef() throws DatabaseException {
		return oid.getChildAt(METHODS_POS);
	}	
	private final OID getSuperRef() throws DatabaseException {
		return oid.getChildAt(SUPER_CLASSES_POS);
	}
	private final OID getInstanceNameRef() throws DatabaseException {
		return oid.getChildAt(INSTANCE_NAME_POS);
	}
	
	private final static int METHODS_POS = 1;
	private final static int SUPER_CLASSES_POS = 2;
	private final static int INSTANCE_NAME_POS = 3;
	
	public final static int FIELD_COUNT = 4;
}
