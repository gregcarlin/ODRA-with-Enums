package odra.db.objects.meta;

import java.nio.ByteBuffer;
import java.util.Date;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.recordtypes.RecordType;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBObject;
import odra.db.objects.data.DataObjectKind;
import odra.db.objects.data.ModuleDumper;
import odra.sbql.results.compiletime.ReferenceSignature;
import odra.sbql.results.compiletime.StructSignature;
import odra.system.Names;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;
import odra.transactions.ast.IASTTransactionCapabilities;
import odra.transactions.metabase.IMBTransactionCapabilities;
import odra.transactions.metabase.MBTransactionCapabilities;

/**
 * This class is responsible for managing metadata. Meta structure:
 * 
 * kind (object kind) metadata (module's metabase, i.e. compiletime objects)
 * compiled metareferences (a list of pointers representing resolved logical
 * names defined in local or imported modules) ref1 ref2 ... uncompiled
 * metareferences (a list of string objects representing logical names efined in
 * local or imported modules) name1 name2 ...
 * 
 * @author murlewski
 */

public class MetaBase extends DBObject {

    /**
     * Initializes a new MetaBase object
     * 
     */
    public MetaBase(OID oid) throws DatabaseException {
	super(oid);
	if (ConfigDebug.ASSERTS)
	    assert oid.isComplexObject();

    }

    /**
     * Initializes a metadata by creating required objects
     * 
     */
    public void initialize() throws DatabaseException {
	store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]),
		oid, DataObjectKind.META_BASE_OBJECT);
	store.createComplexObject(store.addName("$meta"), oid, 0);
	store.createComplexObject(store.addName("$refs"), oid, 0);
	store.createComplexObject(store.addName("$linkedrefs"), oid, 0);

	ByteBuffer buf = ByteBuffer.allocate(Sizes.LONGVAL_LEN);
	OID o = store.createBinaryObject(store.addName("$date"), oid, buf
		.putLong(-1).array(), 0);
    }

    /**
     * @return true if the oid really represents a MetaBase
     */
    public boolean isValid() throws DatabaseException {
	return getObjectKind().getKindAsInt() == DataObjectKind.META_BASE_OBJECT;
    }

    /**
     * Root of the metabase
     * 
     * @return pointer of the metabase entry
     */
    public OID getMetabaseEntry() throws DatabaseException {
	return getMetaBaseRef();
    }

    public OID getMetaBaseReferences() throws DatabaseException {
	return this.getMetaBaseReferencesRef();
    }

    /**
     * Returns oids of objects representing physical references used by the
     * metabase
     */
    public OID[] getCompiledMetaReferences() throws DatabaseException {
	return getCompiledMetaReferencesRef().derefComplex();
    }

    /**
     * Returns the "refnum"th object representing physical reference used by the
     * metabase
     */
    public OID getCompiledMetaReferenceAt(int refnum) throws DatabaseException {
	return getCompiledMetaReferencesRef().getChildAt(refnum);
    }

    final OID getMetaBaseRef() throws DatabaseException {
	return oid.getChildAt(MB_METADATA_POS);
    }

    final OID getMetaBaseReferencesRef() throws DatabaseException {
	return oid.getChildAt(MB_METAREFERENCES_POS);
    }

    public final OID getCompiledMetaReferencesRef() throws DatabaseException {
	return oid.getChildAt(MOD_COMP_META_REFERENCES_POS);
    }

    public final long getSerial() throws DatabaseException {
	ByteBuffer bb = ByteBuffer.wrap(oid.getChildAt(MB_SERIAL_POS)
		.derefBinary());
	long serial = bb.getLong();

	return serial;
    }

    private final void updateSerial() throws DatabaseException {
	ByteBuffer buf = ByteBuffer.allocate(Sizes.LONGVAL_LEN);
	store.updateBinaryObject(oid.getChildAt(MB_SERIAL_POS), buf.putLong(
		new Date().getTime()).array());
    }

    public final void setSerial(long serial) throws DatabaseException {
	ByteBuffer buf = ByteBuffer.allocate(Sizes.LONGVAL_LEN);
	store.updateBinaryObject(oid.getChildAt(MB_SERIAL_POS), buf.putLong(
		serial).array());
    }

    /**
     * records information that the metabase uses a logical reference "refname".
     * the method returns the id of the physical reference.
     */
    public int addMetaReference(String refname) throws DatabaseException {
	OID[] refs = this.getMetaBaseReferencesRef().derefComplex();

	for (int i = 0; i < refs.length; i++)
	    if (refs[i].derefString().equals(refname))
		return i;

	store.createStringObject(store.addName("ref"),
		getMetaBaseReferencesRef(), refname, 0);

	return getMetaBaseReferencesRef().countChildren() - 1;
    }

    /**
     * Creates a physical representation of a logical reference used by the
     * metabase
     */
    public int addCompiledMetaReference(OID ref) throws DatabaseException {
    getCompiledMetaReferencesRef().createPointerChild(store.addName("ref"), ref);

	return getCompiledMetaReferencesRef().countChildren() - 1;
    }

    /**
     * Deletes a logical name from the metabase. Names are not really removed
     * because the numbering scheme of metareferences would be changed. Since
     * some entities may already rely on this order, we only change the name to
     * someting that can surely be bound (void).
     * 
     * @param name
     *                of the reference
     */
    public void removeMetaReference(String refname) throws DatabaseException {
	OID[] refs = this.getMetaBaseReferencesRef().derefComplex();

	for (int i = 0; i < refs.length; i++) {
	    String metarefname = refs[i].derefString();
	    if (metarefname.equals(refname))
		refs[i].updateStringObject("void");
	    else {
		String[] subnames = metarefname.split("\\.");
		if (subnames.length > 0 && subnames[0].equals(refname)) {
		    refs[i].updateStringObject("void");
		}
	    }
	}
    }

    public void removeCompiledMetaReferences() throws DatabaseException {
	getCompiledMetaReferencesRef().deleteAllChildren();
    }

    /**
     * Deletes the entire content of metabase. This method is used during remote
     * metabase refresh to discarded content.
     * 
     * @throws DatabaseException
     */
    void deleteMetaBaseContent() throws DatabaseException {
		getMetaBaseRef().deleteAllChildren();
		getMetaBaseReferencesRef().deleteAllChildren();
		getCompiledMetaReferencesRef().deleteAllChildren();
	
		this.updateSerial();
    }

    /**
     * Returns the "refnum"th object representing logical reference used by the
     * metabase
     */
    public OID getMetaReferenceAt(int refnum) throws DatabaseException {
	return this.getMetaBaseReferencesRef().getChildAt(refnum);
    }

    /**
     * Returns oids of objects representing logical references used by the
     * metabase
     */
    public OID[] getMetaReferences() throws DatabaseException {
	return getMetaBaseReferencesRef().derefComplex();
    }

    OID createComplexObject(String name, OID parent, int buf)
	    throws DatabaseException {
	return store.createComplexObject(store.addName(name), parent, buf);
    }

    /***************************************************************************
     * operations on metabase objects
     * 
     */

    /**
     * Creates a new view in the metabase
     * 
     * @param name
     *                of the view *
     * @param seedsast
     *                serialized ast of the query evaluated in order to generate
     *                seeds of the view
     */
    OID createMetaView(String viewname, String vobjname,
	    String votypename, int mincard, int maxcard, int refind,
	    String seedtypename, byte[] seedast) throws DatabaseException {

	OID viewid = createComplexObject(viewname, getMetaBaseRef(),
		MBView.FIELD_COUNT);
	OID vobjid = createComplexObject(vobjname, getMetaBaseRef(),
		MBVirtualVariable.FIELD_COUNT);

	int votypenameid = addMetaReference(votypename);
	int seedtypenameid = addMetaReference(seedtypename);

	new MBView(viewid).initialize(vobjid, mincard, maxcard, refind,
		seedtypenameid, seedast);
	new MBVirtualVariable(vobjid).initialize(votypenameid, mincard,
		maxcard, refind, viewid);

	this.updateSerial();
	return viewid;
    }

    public OID createMetaInterface(String interfaceName, String instanceName,
	    String[] supint) throws DatabaseException {
	OID typid = this.createMetaStruct(0);
	OID varid = this.createMetaVariable(instanceName, 0, Integer.MAX_VALUE,
		typid.getObjectName(), 0);

	int[] superids = new int[supint.length];
	for (int i = 0; i < supint.length; i++) {
	    superids[i] = this.addMetaReference(supint[i]);
	}

	OID strid = createComplexObject(interfaceName, getMetaBaseRef(),
		MBInterface.FIELD_COUNT);
	new MBInterface(strid).initialize(varid, typid, superids);

	this.updateSerial();

	return strid;
    }

    /**
     * Creates a new class in the metabase
     * 
     * @param name
     *                of the class
     * @param typename
     *                name of the object representing the structure of the class
     *                (possibly an anonymous struct?)
     * @param supernames
     *                table with names of the parent classes (String[0] for no
     *                superclasses)
     */
    OID createMetaClass(String name, String typename, String[] supernames)
	    throws DatabaseException {
	int typeid = this.addMetaReference(typename);
	int[] superids = new int[supernames.length];
	for (int i = 0; i < supernames.length; i++) {
	    superids[i] = this.addMetaReference(supernames[i]);
	}

	OID strid = createComplexObject(name, getMetaBaseRef(),
		MBClass.FIELD_COUNT);
	new MBClass(strid).initialize(typeid, superids);

	this.updateSerial();
	return strid;
    }

    /**
     * Creates a new class in the metabase
     * 
     * @param name
     *                of the class
     * @param invobjname
     *                invariant name of the class instances
     * @param typename
     *                name of the object representing the structure of the class
     *                (possibly an anonymous struct?)
     * @param supernames
     *                table with names of the parent classes (String[0] for no
     *                superclasses)
     */
    OID createMetaClass(String name, String invobjname, String typename,
	    String[] supernames) throws DatabaseException {
	int typeid = this.addMetaReference(typename);
	int[] superids = new int[supernames.length];
	for (int i = 0; i < supernames.length; i++) {
	    superids[i] = this.addMetaReference(supernames[i]);
	}

	OID strid = createComplexObject(name, getMetaBaseRef(),
		MBClass.FIELD_COUNT);
	new MBClass(strid).initialize(typeid, invobjname, superids);

	this.updateSerial();
	return strid;
    }

    /**
     * Creates a new index in the metabase, so that indexes could be used as
     * procedures.
     * 
     * @param name
     *                name of the index
     * @param temporary
     *                true for temporary, false for materialized index TODO:
     *                javadoc
     * 
     */
    public OID createMetaIndex(String name, boolean temporary,
	    StructSignature sign, RecordType recordType, boolean uniqueNonkeys)
	    throws DatabaseException {
	OID varoid = ((ReferenceSignature) sign.getFields()[0]).value;
	OID cpxid = createComplexObject(name, getMetaBaseRef(),
		MBClass.FIELD_COUNT);
	new MBIndex(cpxid).initialize(varoid, temporary, sign, recordType,
		uniqueNonkeys);

	this.updateSerial();
	return cpxid;
    }

    /**
     * Creates a new procedure object in the metabase
     * 
     * @param name
     *                name of the procedure
     * @param mincard
     *                minimum cardinality of the result (see the Bible)
     * @param maxcard
     *                maximum cardinality of the result
     * @param type
     *                name of the result type
     * @param ref
     *                reference indicator of the result (number of &'s)
     * @param argbuf
     *                buffer size for arguments (usually 0)
     * @param astBody
     *                serialized AST of the body
     */
    OID createMetaProcedure(String name, int mincard, int maxcard,
	    String type, int ref, int argbuf, byte[] ast,
	    IASTTransactionCapabilities capsASTTransaction)
	    throws DatabaseException {

	this.updateSerial();
	return this.createMetaProcedure(name, mincard, maxcard, type, ref,
		argbuf, ast, this.getMetaBaseRef(), capsASTTransaction);
    }

    private OID createMetaProcedure(String name, int mincard, int maxcard,
	    String type, int ref, int argbuf, byte[] ast, OID parent,
	    IASTTransactionCapabilities capsASTTransaction)
	    throws DatabaseException {
	int refid = this.addMetaReference(type);

	OID strid = createComplexObject(name, parent, MBProcedure.FIELD_COUNT);
	IMBTransactionCapabilities capsMBTransaction = MBTransactionCapabilities
		.getInstance(capsASTTransaction);
	MBProcedure mbProcedure = new MBProcedure(strid, capsMBTransaction);
	mbProcedure.initialize(refid, mincard, maxcard, ref, argbuf, ast);

	this.updateSerial();
	return strid;
    }

    /**
     * Creates a new variable object in the metabase
     * 
     * @param name
     *                name of the variable
     * @param mincard
     *                minimum cardinality (see the Bible)
     * @param maxcard
     *                maximum cardinality
     * @param type
     *                type of the variable
     * @param ref
     *                reference indicator (number of &'s in the type
     *                specification)
     */
    OID createMetaVariable(String name, int mincard, int maxcard,
	    String type, int ref) throws DatabaseException {
	int typeid = this.addMetaReference(type);

	OID strid = createComplexObject(name, getMetaBaseRef(),
		MBVariable.FIELD_COUNT);
	new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

	this.updateSerial();
	return strid;
    }

    /**
     * Creates a new annotated variable object in the metabase
     * 
     * @param name
     *                name of the variable
     * @param mincard
     *                minimum cardinality (see the Bible)
     * @param maxcard
     *                maximum cardinality
     * @param type
     *                type of the variable
     * @param ref
     *                reference indicator (number of &'s in the type
     *                specification)
     */
    OID createMetaAnnotatedVariable(String name, int mincard,
	    int maxcard, String type, int ref) throws DatabaseException {
	int typeid = this.addMetaReference(type);

	OID strid = createComplexObject(name, this.getMetaBaseRef(),
		MBVariable.FIELD_COUNT);
	new MBAnnotatedVariableObject(strid).initialize(mincard, maxcard, type,
		ref);

	this.updateSerial();
	return strid;
    }


    /**
     * Creates a new typedef object in the metabase (possibly distinct).
     * 
     * @param name
     *                name of the typedef
     * @param typename
     *                name of the base type (possibly an anonymous struct?)
     * @param isDistinct
     *                indicates whether the typedef introduces a distinct type
     */
    OID createMetaTypeDef(String name, String typename,
	    boolean isDistinct) throws DatabaseException {
	int typeid = this.addMetaReference(typename);

	OID typedefid = createComplexObject(name, this.getMetaBaseRef(),
		MBTypeDef.FIELD_COUNT);
	new MBTypeDef(typedefid).initialize(typeid, isDistinct);

	this.updateSerial();
	return typedefid;
    }

	public OID createExternalSchemaDef(String name) throws DatabaseException {
		//int typeid = this.addMetaReference(typename);

		OID externalschemadefid = createComplexObject(name, this.getMetaBaseRef(),
			MBSchema.FIELD_COUNT);
		new MBSchema(externalschemadefid).initialize();

		this.updateSerial();
		return externalschemadefid;
	}
    
    /**
     * Creates a new anonymous (not available for binding) struct in the
     * metabase.
     * 
     * @param fieldbuf
     *                buffer size for subobjects
     */
    OID createMetaStruct(int fieldbuf) throws DatabaseException {
	String name;

	// find a unique name for the structure being created
	int nameid;
	do {
	    name = "$struct_" + System.currentTimeMillis();
	    nameid = store.getNameId(name);
	} while (nameid != -1);

	this.updateSerial();
	return createMetaStruct(name, 0);
    }

    /**
     * Creates a new, named struct object in the metabase.
     * 
     * @param name
     *                name of the struct
     * @param fieldbuf
     *                buffer size for subobjects
     */
    private OID createMetaStruct(String name, int fieldbuf)
	    throws DatabaseException {
	OID strid = createComplexObject(name, this.getMetaBaseRef(), 0);
	new MBStruct(strid).initialize(fieldbuf);

	this.updateSerial();
	return strid;
    }
    
    public OID createMetaEnum(String name,String typename, int fieldbuf) 
	throws DatabaseException {
	
    int typeidenu = this.addMetaReference(name);
	int typeid = this.addMetaReference(typename);
	OID enuid = createComplexObject(name, this.getMetaBaseRef(), 0);
	new MBEnum(enuid).initialize(typeid,fieldbuf);

	this.updateSerial();
	return enuid;
    }



    public OID findFirstByName(String name, OID parent)
	    throws DatabaseException {
	return store.findFirstByNameId(store.addName(name), parent);
    }

    public OID findFirstByNameId(int nameid, OID parent)
	    throws DatabaseException {
	return store.findFirstByNameId(nameid, parent);
    }

    public String dump(DBModule module, String indend) throws DatabaseException {
	int mobjnameid = oid.getObjectNameId();
	String mobjname = store.getName(mobjnameid);

	String metastr = "\t" + oid.toString() + "\t\t" + indend + "#"
		+ mobjnameid + " (" + mobjname + ") [Metabase] serial "
		+ getSerial() + "\n";

	ModuleDumper dumper = new ModuleDumper(module);

	OID[] metaoids = this.getMetabaseEntry().derefComplex();

	for (int j = 0; j < metaoids.length; j++)
	    metastr += dumper.dumpMetadata(module, metaoids[j], indend + " ");

	return metastr;
    }

    public DBModule getDBModuleOwner() throws DatabaseException {
	DBModule module;
	OID moid = oid;

	do {
	    moid = moid.getParent();

	    if (ConfigDebug.ASSERTS)
		assert moid != null : "parent object expected";
	} while (moid.countChildren() < 2 || !new DBModule(moid).isValid());

	module = new DBModule(moid);

	if (module == null && ConfigDebug.ASSERTS)
	    ConfigServer.getLogWriter().getLogger().log(
		    java.util.logging.Level.SEVERE, "Couldn't find a module");

	return module;
    }

    /**
     * Verifies if metabase conforms to the given metabase 
     * @param metaBase required schema metabase 
     */
    public boolean comformsTo(MetaBase metaBase) {
		// TODO Auto-generated method stub
    	
		return false;
	}
    
    protected final static int MB_METADATA_POS = 1;

    protected final static int MB_METAREFERENCES_POS = 2;

    protected final static int MOD_COMP_META_REFERENCES_POS = 3;

    protected final static int MB_SERIAL_POS = 4;

    public final static int FIELD_COUNT = 5;

}
