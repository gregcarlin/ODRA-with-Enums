package odra.db.objects.meta;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase
 * objects representing classes.
 * 
 * @author raist, radamus
 */

public class MBClass extends MBObject {
	/**
	 * Initializes a new MBClass object
	 * @param oid OID of an existing class object or an empty complex object
	 */
	public MBClass(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();		
	}

	/**
	 * Initializes the metaclass using the id of its type
	 * (no invariant name, no superclasses)
	 * @param structid identifier of the name being the type of the class (position in the table of metareferences)
	 */	
	public void initialize(int typeid) throws DatabaseException {
		initialize(typeid, NO_INSTANCE_NAME, new int[0]);
	}

	/**
	 * Initializes the metaclass using the id of its type and  invariant name
	 * (no superclasses)
	 * @param structid identifier of the name being the type of the class (position in the table of metareferences)
	 */	
	public void initialize(int typeid, String invariantname) throws DatabaseException {
		initialize(typeid, store.addName(invariantname), new int[0]);
	}
	
	/**
	 * Initializes the metaclass using an id of its type, and an id of its superclass
	 * (no invariant name)
	 */
	public void initialize(int typeid, int superid) throws DatabaseException {
		
		initialize(typeid, NO_INSTANCE_NAME, new int[] {superid});
		
	}
	
	/**
	 * Initializes the metaclass using an id of its type, and an ids of its superclasses
	 * (no invariant name)
	 */
	public void initialize(int typeid, int[] superids) throws DatabaseException {
		
		initialize(typeid, NO_INSTANCE_NAME, superids);
		
	}

	/**
	 * Initializes the metaclass using an id of its type, invariant name and an ids of its superclasses
	 * (for inheritance)
	 */
	public void initialize(int typeid, String invariantname, int[] superids) throws DatabaseException {
		initialize(typeid, store.addName(invariantname), superids);
	}
	
	/**
	 * Initializes the metaclass using an id of its type id of invariant name and an ids of its superclasses
	 * 
	 */
	private void initialize(int typeid, int instancenameid, int[] superids) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.CLASS_OBJECT.kindAsInt());
		store.createComplexObject(store.addName(Names.namesstr[Names.EXTENDS_ID]), oid, 0);
		store.createIntegerObject(store.addName(Names.namesstr[Names.STRUCTURE_ID]), oid, typeid);
		store.createComplexObject(store.addName(Names.namesstr[Names.METHODS_ID]), oid, 0);
		OID soid = store.createComplexObject(instancenameid, oid, MBVariable.FIELD_COUNT);
		this.initializeSelfInstanceVariable(soid);
		OID doid = store.createComplexObject(instancenameid, oid, MBVariable.FIELD_COUNT);
		this.initializeDefaultInstanceVariable(doid);
		for(int superid:superids)
			store.createIntegerObject(store.addName(Names.namesstr[Names.SUPER_ID]), getExtendsRef(), superid);
	}
	

	/**
	 * Does the oid really represent a procedure?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.CLASS_OBJECT;
	}
	
	
	/**
	 * Superclasses
	 */
	public OID[] getDirectSuperClasses() throws DatabaseException {
		DBModule module = getModule();
		MetaBase metaBase = getMetaBase();
		
		if (ConfigDebug.ASSERTS) assert module.isModuleLinked() : "uncompiled module";		
		
		OID[] extendsoids = getExtendsRef().derefComplex();
		OID[]superclasses = new OID[extendsoids.length];
		for(int i = 0; i < extendsoids.length; i++){
			superclasses[i] = metaBase.getCompiledMetaReferenceAt(extendsoids[i].derefInt()).derefReference();
		}
		return superclasses;
	}
	/**
	 * @return position of the type name in the list of logical/physical module references
	 */
	public int getClassTypeNameId() throws DatabaseException {
		return getStructRef().derefInt();
	}
	/**
	 * OID to a structure representing type  of the class instance
	 * (direct type)
	 */
	public OID getType() throws DatabaseException {
		DBModule module = getModule();
		
		if (ConfigDebug.ASSERTS) assert module.isModuleLinked() : "uncompiled module";

		int typeid = getStructRef().derefInt();

		return getMetaBase().getCompiledMetaReferenceAt(typeid).derefReference();
	}
	
	/**
	 * Vector with OIDs of structures representing type invariants of the class superclasses
	 */
	public Vector<OID> getInheritedTypes() throws DatabaseException {
		DBModule module = getModule();
		if (ConfigDebug.ASSERTS) assert module.isModuleLinked() : "uncompiled module";
		Vector<OID> types = new Vector<OID>();
		for(OID superclsid: this.getDirectSuperClasses()){
			MBClass supecls = new MBClass(superclsid);
			Vector<OID> superclstypes = supecls.getInheritedTypes();
			for(OID superclstype:superclstypes){
				if(!types.contains(superclstype))
					types.add(superclstype);
			}
			types.add(supecls.getType());
			
		}
		
		return types;
	}
	
	/**
	 * Vector with OIDs of structures representing type invariants of the class (superclasses + direct type)
	 */
	public Vector<OID> getFullType() throws DatabaseException {
		Vector<OID> types = this.getInheritedTypes();
		types.add(this.getType());
		return types;
	}
	/**
	 * @return true if class has invariant name
	 * @throws DatabaseException
	 */
	public boolean hasInstanceName() throws DatabaseException{
		return this.getSelfRef().getObjectNameId() != NO_INSTANCE_NAME; 
	}
	
	/** Checks if the class is a sub-class of a param class
	 * @param clsid - oid of the meta-class 
	 * @return true if this meta class is sub-class of the 'clsid'
	 * false otherwise
	 * @throws DatabaseException
	 */
	public boolean isSubClassOf(OID clsid)throws DatabaseException{
		assert new MBClass(clsid).isValid() : "meta class required";
		if(clsid.equals(this.oid)) return true;
	    	for(OID scls: this.getDirectSuperClasses()){
			if(scls.equals(clsid))
				return true;
			if(new MBClass(scls).isSubClassOf(clsid)){
			    return true;
			}
		}
		return false;
	}
	
	/** Checks if the class have common super-class with a param 
	 * @param clsid - oid of the meta-class
	 * @return true if this meta class has common super-class with the 'clsid'
	 * false otherwise
	 * @throws DatabaseException
	 */
	public boolean haveCommonSuperClass(OID clsid)throws DatabaseException{
	    assert new MBClass(clsid).isValid() : "meta class required";
	    OID[] thisSuper = this.getDirectSuperClasses();
	    OID[] paramSuper = new MBClass(clsid).getDirectSuperClasses();
		
	    for(int i = 0; i < thisSuper.length; i++){
		for(int j = 0; j < paramSuper.length; j++){
		    if(thisSuper[i].equals(paramSuper[j]))
			return true;
		}
	    }
	    return false;
	}
	
	/**
	 * @return name of the type
	 */
	public String getStructureTypeName() throws DatabaseException
	{
		return getMetaBase().getMetaReferenceAt( getStructRef().derefInt() ).derefString();
	}
	
	/**
	 * @return class invariant name or null (if class does not have one)
	 * @throws DatabaseException
	 */
	public String getInstanceName() throws DatabaseException{
		int nameid = this.getSelfRef().getObjectNameId();
		if (nameid == NO_INSTANCE_NAME)
			return null;
		return store.getName(nameid);
	}
	
	public OID getSelfVariable() throws DatabaseException{
		return this.getSelfRef();
	}
	public OID getDefaultVariable() throws DatabaseException{
		return this.getDefaultVariableRef();
	}
	/* (non-Javadoc)
	 * @see odra.db.objects.meta.MBObject#getNestedMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
	    	Vector<OID> types = this.getFullType();
	    	OID[] metaentries = new OID[types.size()]; 
	    	for(int i = 0; i < types.size(); i++){
	    	    metaentries[i] = new MBStruct(types.get(i)).getNestedMetabaseEntries()[0];
	    	}
		return metaentries;
	}
	/**
	 * OIDs of procedures being class methods
	 */
	public OID[] getMethods() throws DatabaseException {
		return getMethodsRef().derefComplex();
	}
	
	/**
	 * OID of class methods entry
	 */
	public OID getMethodsEntry() throws DatabaseException {
		return getMethodsRef();
	}
	
	/**
	 * Creates a new method
	 */
	OID createMethod(String name, int mincard, int maxcard, String type,  int ref, int argbuf, byte[] ast) throws DatabaseException {		
	
		MetaBase metaBase = getMetaBase(); 
		
		int refid = metaBase.addMetaReference(type);
		
		OID strid = store.createComplexObject(store.addName(name), getMethodsRef(), 4);
		new MBProcedure(strid).initialize(refid, mincard, maxcard, ref, argbuf, ast);

		return strid;
	}
	
	private void initializeSelfInstanceVariable(OID soid) throws DatabaseException{
	    int typeid = getMetaBase().addMetaReference(oid.getObjectName());
	    MBVariable self = new MBVariable(soid);
	    self.initialize(typeid, 1, 1, 0);
	    
	}
	/**
	 * @param doid
	 */
	private void initializeDefaultInstanceVariable(OID doid) throws DatabaseException{
		int typeid = getMetaBase().addMetaReference(oid.getObjectName());
	    MBVariable self = new MBVariable(doid);
	    self.initialize(typeid, 0, Integer.MAX_VALUE, 0);
		
	}
	/***********************************
	 * debugging
	 * */
	public String dump(String indend) throws DatabaseException {

		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();
		MetaBase metaBase = getMetaBase();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
		//invariant name
		if(this.hasInstanceName()){
			metastr += ", instance: " + this.getInstanceName();
		}
		// type
		int structid = getStructRef().derefInt();		
		metastr += ", structure: " + getStructureTypeName();

		// extends
		OID[] extend = getExtendsRef().derefComplex();
		if (extend.length > 0) {
			metastr += ", extends: ";
			for (int i = 0; i < extend.length; i++) {
				metastr += getMetaBase().getMetaReferenceAt(extend[i].derefInt()).derefString();
				
				if (i < extend.length - 1)
					metastr += ", ";
			}
		}
		metastr += " [class]\n";

		// methods
		OID[] methods = getMethodsRef().derefComplex();
		for (int i = 0; i < methods.length; i++)
			metastr += new MBProcedure(methods[i]).dump(indend + " mth. " + i + ": ");

		return metastr;
	}
	
	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getExtendsRef() throws DatabaseException {
		return oid.getChildAt(EXTENDS_POS);
	}

	private final OID getStructRef() throws DatabaseException {
		return oid.getChildAt(STRUCTURE_POS);
	}

	private final OID getMethodsRef() throws DatabaseException {
		return oid.getChildAt(METHODS_POS);
	}

	private final OID getSelfRef()throws DatabaseException{
		return oid.getChildAt(SELF_POS);
	}
	
	private final OID getDefaultVariableRef()throws DatabaseException{
		return oid.getChildAt(DEFAULT_VARIABLE);
	}
	
	private final static int EXTENDS_POS = 1;
	private final static int STRUCTURE_POS = 2;
	private final static int METHODS_POS = 3;
	private final static int SELF_POS = 4;
	private final static int DEFAULT_VARIABLE = 5;

	private final static int NO_INSTANCE_NAME = -1;

	public final static int FIELD_COUNT = 6;
}
