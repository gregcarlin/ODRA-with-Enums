package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.ModuleDumper;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase objects
 * representing structures.
 * 
 * @author raist
 */

public class MBStruct extends MBObject {
	/**
	 * Initializes the MBStruct object
	 * @param oid OID of an existing structure or an empty complex object
	 */
	public MBStruct(OID oid) throws DatabaseException {
		super(oid);
		
		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}
	
	/**
	 * Initializes the structure in the metabase by creating some system-level subobjects.
	 * @param size size of a buffer used to store fields (can be 0)
	 */
	public void initialize(int fieldbuf) throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.STRUCT_OBJECT.kindAsInt());
		store.createComplexObject(store.addName(Names.namesstr[Names.FIELDS_ID]), oid, fieldbuf);
		store.createComplexObject(store.addName(Names.namesstr[Names.METHODS_ID]), oid, fieldbuf);
	}

	/**
	 * @return true if the oid really represents a structure
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.STRUCT_OBJECT;
	}

	/**
	 * Adds a new field to the structure
	 * @see MBVariable
	 */
	// FIXME: type as nameid
	public OID createField(String name, int mincard, int maxcard, String type, int ref) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		int typeid = metaBase.addMetaReference(type);

		OID strid = metaBase.createComplexObject(name, getFieldsRef(), MBVariable.FIELD_COUNT);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);
	
		return strid;
	}
	
	OID createProcedure(String procname, String typename, int mincard, int maxcard, int ref) throws DatabaseException {		
		MetaBase metaBase = getMetaBase();
		int typeid = metaBase.addMetaReference(typename);
		
		OID oid = store.createComplexObject(store.addName(procname), this.getProceduresRef(), mincard);
		new MBProcedure(oid).initialize(typeid, mincard, maxcard, ref, 0, new byte[0]);

		return oid;
	}
	
	/**
	 * Adds a new field representing binary association 
	 * @see MBVariable
	 */
	// FIXME: type as nameid
	public OID createBinaryAssociationField(String name, int mincard, int maxcard, String type, int ref, String revname) throws DatabaseException {
		MetaBase metaBase = getMetaBase();
		int typeid = metaBase.addMetaReference(type);
		int revnameid = metaBase.addMetaReference(type + "." + revname);

		OID strid = metaBase.createComplexObject(name, getFieldsRef(), MBVariable.FIELD_COUNT);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref, revnameid);
	
		return strid;
	}
	/**
	 * @return references to objects representing fields of the structure
	 */
	public OID[] getFields() throws DatabaseException {
		return getFieldsRef().derefComplex();
	}
	
	public OID[] getProcedures() throws DatabaseException {
		return getProceduresRef().derefComplex();
	}
	
	/**
	 * @param name - name of the searche field
	 * @return - structure field with a given name if found, null otherwise
	 * @throws DatabaseException
	 */
	public OID findFieldByName(String name) throws DatabaseException{
	    return this.getModule().findFirstByName(name, getFieldsRef());
	}
	/* (non-Javadoc)
	 * @see odra.db.objects.meta.MBObject#getSubMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
		// TODO Auto-generated method stub
		return new OID[] {getFieldsRef()};
	}

	/***********************************
	 * debugging
	 * */
	
	public String dump(String indend) throws DatabaseException {
		
		int mobjnameid = oid.getObjectNameId();
		String mobjname = oid.getObjectName();

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ") [struct]\n";

		ModuleDumper dumper = new ModuleDumper(getModule());
		
		OID[] strchildren = getFieldsRef().derefComplex();
		for (int j = 0; j < strchildren.length; j++)
			metastr += dumper.dumpMetadata(getModule(), strchildren[j], indend + " fld. " + j + ": ");	

		OID[] strprocs = getProceduresRef().derefComplex();
		for (int k = 0; k < strprocs.length; k++)
			metastr += dumper.dumpMetadata(getModule(), strprocs[k], indend + " prc. " + k + ": ");	

		return metastr;
	}

	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getFieldsRef() throws DatabaseException {
		return oid.getChildAt(FIELDS_POS);
	}
	
	private final OID getProceduresRef() throws DatabaseException {
		return oid.getChildAt(METHODS_POS);
	}
	
	private final static int FIELDS_POS = 1;
	private final static int METHODS_POS = 2;
	
	public final static int FIELD_COUNT = 3;
}
