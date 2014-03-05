package odra.db.objects.meta;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.IMetaBaseHolder;
import odra.system.Names;
import odra.system.config.ConfigDebug;

/**
 * This class provides API for operations on metabase objects
 * representing database links (used in distributed communication).
 * 
 * @author raist
 */

public class MBLink extends MBObject implements IMetaBaseHolder{
	/**
	 * Initializes a new MBLink object
	 * @param oid OID of an existing MBLink object
	 */
	public MBLink(OID oid) throws DatabaseException {
		super(oid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject() : "oid.isComplexObject() == true" +  oid.getObjectName() + " " + oid.getObjectKind().toString();
	}

	/**
	 * Initializes the link.
	 * @param host target host
	 * @param port target database listener's port
	 * @param target schema
	 */
	public void initialize(String host, int port, String schema) throws DatabaseException {	
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, MetaObjectKind.LINK_OBJECT.kindAsInt());
		store.createStringObject(store.addName(Names.namesstr[Names.HOST_ID]), oid, host, 0);
		store.createIntegerObject(store.addName(Names.namesstr[Names.PORT_ID]), oid, port);
		store.createStringObject(store.addName(Names.namesstr[Names.SCHEMA_ID]), oid, schema, 0);
		
		OID metaOID = store.createComplexObject(store.addName("$metabase"), oid, 0);
		new MetaBase(metaOID).initialize();			
	}

	/**
	 * @return true if the oid really represent a meta database link?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind() == MetaObjectKind.LINK_OBJECT;
	}

	/**
	 * @return name of the host at which the link points
	 */
	public String getHost() throws DatabaseException {
		return getHostRef().derefString();
	}
	
	/**
	 * @return target database port at which the link points
	 */
	public int getPort() throws DatabaseException {
		return getPortRef().derefInt();
	}
	
	/**
	 * @return target database user's schema at which the link points
	 */
	public String getSchema() throws DatabaseException {
		return getSchemaRef().derefString();
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
		metastr += " [" + getSchemaRef().derefString() + "@" + getHostRef().derefString() +":" + getPortRef().derefInt()  + "[database link]\n";
		
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
	

	/***********************************
	 * access to subobjects describing the declaration
	 * */

	private final OID getHostRef() throws DatabaseException {
		return oid.getChildAt(HOST_POS);
	}

	private final OID getPortRef() throws DatabaseException {
		return oid.getChildAt(PORT_POS);
	}

	private final OID getSchemaRef() throws DatabaseException {
		return oid.getChildAt(SCHEMA_POS);
	}

	private final OID getMetaBaseRef() throws DatabaseException {
		return oid.getChildAt(MOD_METADATA_POS);
	}
	
	private final static int HOST_POS = 1;
	private final static int PORT_POS = 2;
	private final static int SCHEMA_POS = 3;
	private final static int MOD_METADATA_POS = 4;
	
	public final static int FIELD_COUNT = 5;
}
