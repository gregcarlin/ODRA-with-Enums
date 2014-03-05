package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.IMetaBaseHolder;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase objects
 * representing database schema (used in distributed communication).
 * 
 * @author raist
 */

public class MBSchema extends MBObject implements IMetaBaseHolder{
	/**
	 * Initializes a new MBSchema object
	 * @param oid OID of an existing MBSchema object
	 */
	public MBSchema(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the meta-schema.
	 */
	public void initialize() throws DatabaseException {	
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.LINK_OBJECT.kindAsInt());
		
		OID metaOID = store.createComplexObject(store.addName("$metabase"), oid, 0);
		new MetaBase(metaOID).initialize();			
	}

	/**
	 * @return true if the oid really represent a meta database link?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.SCHEMA_OBJECT;
	}
	
	/**
	 * @return the MetaBase associated with Link
	 * @throws DatabaseException
	 */
	public MetaBase getMetaBase() throws DatabaseException{
		return new MetaBase(getMetaBaseRef());
	}
	
	/* (non-Javadoc)
	 * @see odra.db.objects.meta.MBObject#getNestedMetabaseEntry()
	 */
	@Override
	public OID[] getNestedMetabaseEntries() throws DatabaseException {
	    
	    return new OID[] {this.getMetaBase().getMetabaseEntry()};
	}

	/***********************************
	 * debugging
	 * */

	public String dump(String indend) throws DatabaseException {
		int mobjnameid = oid.getObjectNameId();
		String mobjname = store.getName(mobjnameid);

		String metastr = "\t" + oid.toString() + "\t\t" + indend + "#" + mobjnameid + " (" + mobjname + ")";
	
		// dump links Metareferences
		metastr += "\t\t\t" + indend + "Link : " + mobjname + " Metareferences:\n";
		OID[] metarefs = getMetaBase().getMetaBaseReferences().derefComplex();
		for (int i = 0; i < metarefs.length; i++)
			metastr += "\t\t\t" + indend +  i + ":\t" + metarefs[i].derefString() + "\n";

		
		// dump links Compiled metareferences
		metastr += "\t\t\t" + indend + "Link : " + mobjname + " Compiled metareferences:\n";
		OID[] cmetarefs = getMetaBase().getCompiledMetaReferencesRef().derefComplex();
		for (int i = 0; i < cmetarefs.length; i++)
			metastr += "\t\t\t" + indend +  i + ":\t&" + cmetarefs[i].derefReference().toString() +"\n";
		
		// dump link metadata
		metastr += "\t\t\t" + indend + "Link : " + mobjname + " Metadata:\n";
		metastr += getMetaBase().dump(getModule()," ");
		
		return metastr;
	}
	

	/***********************************************************************************************
	 * access to subobjects describing the declaration
	 */
	
	private final OID getMetaBaseRef() throws DatabaseException {
		return oid.getChildAt(METADATA_POS);
	}
	
	private final static int METADATA_POS = 1;
	
	public final static int FIELD_COUNT = 2;
}
