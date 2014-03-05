package odra.db.objects.data;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.structures.IndexStructureKind;
import odra.db.links.LinkManager;
import odra.db.objects.IMetaBaseHolder;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaBase;
import odra.exceptions.rd.RDException;
import odra.sbql.results.compiletime.StructSignature;
import odra.sessions.Session;
import odra.system.Names;
import odra.system.config.ConfigDebug;
import odra.transactions.ast.IASTTransactionCapabilities;
import odra.virtualnetwork.cmu.CMUnit;
import odra.virtualnetwork.pu.ClientUnit;
import odra.wrapper.Wrapper;

/**
 * The class is responsible for creating, deleting, compiling, integrating, etc. of database modules.
 * <br><br>
 * Module structure:
 * <br><br>
 * kind (object kind)<br> name (global module name)<br> compiled (indicates if the module has been linked, that is all logical
 * names have been resolved to runtime objects)<br> compiled imports (a list of pointers representing resolved logical names
 * of imported modules)<br> ref1 <br> ref2 <br> ... <br> uncompiled imports (a list of string objects representing logical names of
 * imported modules)<br> name1 <br> name2 <br> ... <br> metadata (module's metabase)<br> ... <br> data (module's database, i.e. runtime objects)<br> ...
 * <br>submodules <br> module <br> module <br> ... <br>
 * 
 * !!!03.03.2007 radamus added session specific subobjects!!! session metadata (module session meta (compile time)
 * objects) ...
 * 
 * init (bytecode for module's runtime session environment initialization)
 */

public class DBModule extends DBObject implements IMetaBaseHolder{
	/********************************************************************************************************************
	 * module construction
	 */

	/**
	 * Creates a new DBModule object. It doesn't create a module in the database, but uses an existing one. If you want
	 * to construct a new module, you need to create a complex object, pass its oid to the constructor and call
	 * initialize()
	 * 
	 * @param oid
	 *           of a module
	 */
	public DBModule(OID moid) throws DatabaseException {
		super(moid);

		if (ConfigDebug.ASSERTS) assert oid.isComplexObject();
	}

	/**
	 * Initializes a module structure using by creating some special-purpose objects and connecting them to an existing,
	 * empty complex object
	 */
	public void initialize() throws DatabaseException {
		store.createIntegerObject(store.addName(Names.namesstr[Names.KIND_ID]), oid, DataObjectKind.MODULE_OBJECT);
		store.createStringObject(store.addName("$name"), oid, determineGlobalName(), 0);
		store.createStringObject(store.addName("$schema"), oid, determineSchema(), 0);
		store.createBooleanObject(store.addName("$compiled"), oid, false);
		store.createBooleanObject(store.addName("$linked"), oid, false);
		store.createComplexObject(store.addName("$imports"), oid, 0);
		store.createComplexObject(store.addName("$importaliases"), oid, 0);
		store.createComplexObject(store.addName("$implements"), oid, 0);
		store.createComplexObject(store.addName("$linkedimports"), oid, 0);
		OID metaOID = store.createComplexObject(store.addName("$metabase"), oid, 0);
		store.createComplexObject(store.addName("$data"), oid, 0);
		store.createComplexObject(store.addName("$submodules"), oid, 0);
		store.createComplexObject(store.addName("$sessionmetadata"), oid, 0);
		store.createBinaryObject(store.addName("$init"), oid, new byte[0], 0);

		new MetaBase(metaOID).initialize();
	}

	/**
	 * Checks if the oid really points at a module?
	 */
	public boolean isValid() throws DatabaseException {
		return getObjectKind().getKindAsInt() == DataObjectKind.MODULE_OBJECT;
	}

	/**
	 * Root of the runtime content stored in the module
	 * 
	 * @return pointer of the database entry
	 */
	public OID getDatabaseEntry() throws DatabaseException {
		return getDataRef();
	}

	/**
	 * Root of the compile time content stored in the module
	 * 
	 * @return pointer of the metabase entry
	 */
	public OID getMetabaseEntry() throws DatabaseException {
		return getMetaRef();

	}

	public String getSchema() throws DatabaseException {
		return this.getSchemaRef().derefString();
	}

	/**
	 * Returns the OID of the module
	 */
	public OID getOID() {
		return oid;
	}

	/**
	 * Name of the module with names of parent modules separated by dots
	 */
	public String getModuleGlobalName() throws DatabaseException {
		return getNameRef().derefString();
	}

	/**
	 * False if there are logical references to be resolved or procedures to be compiled
	 */
	public boolean isModuleCompiled() throws DatabaseException {
		return getCompiledRef().derefBoolean();
	}

	/**
	 * Sets the compilation flag to true. A module is compiled when it is linked to other modules, i.e. all logical names
	 * (e.g. string) are bound to corresponding entities. The entities may be in other modules.
	 */
	public void setModuleCompiled(boolean flag) throws DatabaseException {
		store.updateBooleanObject(getCompiledRef(), flag);
	}

	/**
	 * False if there are logical references to be resolved or procedures to be compiled.
	 */
	public boolean isModuleLinked() throws DatabaseException {
		return getLinkedRef().derefBoolean();
	}

	/**
	 * Sets the compilation flag to true. a module is compiled when it is linked to other modules, i.e. all logical names
	 * (e.g. string) are bound to corresponding entities. the entities may be in other modules.
	 */
	public void setModuleLinked(boolean flag) throws DatabaseException {
		store.updateBooleanObject(getLinkedRef(), flag);
	}

	protected String determineSchema() throws DatabaseException {
		String schema = "admin";

		if (Database.getSystemModule() != null && oid.getParent() != null) {
			OID sysmod = Database.getSystemModule().getOID();
			OID curmod = oid;

			while (curmod.getParent().getParent() != null && !curmod.getParent().getParent().equals(sysmod))
				curmod = curmod.getParent().getParent();

			schema = curmod.getObjectName();
		}

		return schema;
	}

	/**
	 * Determines the global name of the module by checking its parent modules
	 */
	protected String determineGlobalName() throws DatabaseException {
		String globalName = "";

		if (oid.equals(Database.getSystemModule().getOID())) globalName = "system";
		else {
			OID curmod = oid;
			OID sysmod = Database.getSystemModule().getOID();

			while (!curmod.equals(sysmod)) {
				globalName = new DBModule(curmod).getName() + globalName;

				curmod = curmod.getParent().getParent();

				if (!curmod.equals(sysmod)) globalName = "." + globalName;
			}
		}

		return globalName;
	}

	/*
	 * operations on related modules
	 */

	/**
	 * Oids to modules being subordinates of this module
	 */
	public OID[] getSubmodules() throws DatabaseException {
		return getSubmodulesRef().derefComplex();
	}

	/**
	 * Oid of the submodule with local name 'name'
	 */
	public OID getSubmodule(String name) throws DatabaseException {
		return findFirstByName(name, this.getSubmodulesRef());
	}

	/**
	 * Returns modules that have references pointing at this module.
	 */
	public OID[] getDependendModules() throws DatabaseException {
		return oid.getReferencesPointingAt();
	}

	/*
	 * delete compiled references
	 */

	/**
	 * Deletes physical references created by resolving names of imported modules
	 */
	public void removeCompiledImports() throws DatabaseException {
		getCompiledImportsRef().deleteAllChildren();
	}
	
	/**
	 * Deletes physical references created by resolving names of metabase entities
	 */
	public void removeCompiledMetaReferences() throws DatabaseException {
		this.getMetaBase().removeCompiledMetaReferences();
	}

	/*
	 * delete logical references
	 */

	/**
	 * Deletes a logical name from the metabase. Names are not really removed because the numbering scheme of
	 * metareferences would be changed. Since some entities may already rely on this order, we only change the name to
	 * someting that can surely be bound (void).
	 * 
	 * @param name
	 *           of the reference
	 */
	public void removeMetaReference(String refname) throws DatabaseException {
		getMetaBase().removeMetaReference(refname);
	}

	/**
	 * Deletes logical names of imported modules.
	 */
	public void removeImports() throws DatabaseException {
		getImportsRef().deleteAllChildren();
		getImportsAliasesRef().deleteAllChildren();

		addImport("system");
	}

	/*
	 * add new logical references
	 */

	/**
	 * Records a new record about a module that the current module imports
	 */
	public int addImport(String impname) throws DatabaseException {
		return this.addImport(impname, "");
	}
	
	/**
	 * Records a new record about a module that the current module imports
	 * @param impname - global name of the imported module
	 * @param alias - alias name for the imported module
	 */
	public int addImport(String impname, String alias) throws DatabaseException {
		OID[] refs = this.getImportsRef().derefComplex();

		for (int i = 0; i < refs.length; i++)
			if (refs[i].derefString().equals(impname)) return i;

		store.createStringObject(store.addName("import"), getImportsRef(), impname, 0);
		store.createStringObject(store.addName("importalias"), getImportsAliasesRef(), alias, 0);

		return getImportsRef().countChildren() - 1;
	}

	public int addImplement(String impname) throws DatabaseException {
		OID[] refs = this.getImplementsRef().derefComplex();

		for (int i = 0; i < refs.length; i++)
			if (refs[i].derefString().equals(impname)) return i;

		store.createStringObject(store.addName("implement"), getImplementsRef(), impname, 0);

		return getImplementsRef().countChildren() - 1;
	}
	
	public OID[] getImplements() throws DatabaseException {
		return getImplementsRef().derefComplex();
	}

	/**
	 * records information that the metabase uses a logical reference "refname". the method returns the id of the
	 * physical reference.
	 */
	public int addMetaReference(String refname) throws DatabaseException {
		return getMetaBase().addMetaReference(refname);
	}

	/*
	 * add compiled references
	 */

	/**
	 * Records information about the physical localization of a particular module
	 */
	public int addCompiledImport(OID impoid) throws DatabaseException {
		getCompiledImportsRef().createReferenceChild(store.addName("import"), impoid);

		return getCompiledImportsRef().countChildren() - 1;
	}

	/**
	 * Creates a physical representation of a logical reference used by the metabase
	 */
	public int addCompiledMetaReference(OID ref) throws DatabaseException {
		return getMetaBase().addCompiledMetaReference(ref);
	}

	/*
	 * return logical references
	 */

	/**
	 * Returns oids of objects representing the import list
	 */
	public OID[] getImports() throws DatabaseException {
		return getImportsRef().derefComplex();
	}
	/**
	 * Returns oids of string objects representing the import alias names list
	 */
	public OID[] getImportsAliases() throws DatabaseException {
		return getImportsAliasesRef().derefComplex();
	}
	/**
	 * Returns oids of objects representing logical references used by the metabase
	 */
	public OID[] getMetaReferences() throws DatabaseException {
		return getMetaBase().getMetaReferences();
	}

	/**
	 * Returns the "refnum"th object representing logical reference to a module
	 */
	public OID getImportAt(int refnum) throws DatabaseException {
		return getMetaReferencesRef().getChildAt(refnum);
	}
	/**
	 * Returns the "refnum"th string object representing imported module alias name
	 * if the alias name was not set the value of the object is an empty string
	 */
	public OID getImportAliasNameAt(int refnum) throws DatabaseException {
		return getMetaReferencesRef().getChildAt(refnum);
	}
	/**
	 * Returns the "refnum"th object representing logical reference used by the metabase
	 */
	public OID getMetaReferenceAt(int refnum) throws DatabaseException {
		return getMetaBase().getMetaReferenceAt(refnum);

	}

	/**
	 * Returns oids of objects representing physical references imported by the module
	 */
	public OID[] getCompiledImports() throws DatabaseException {
		return getCompiledImportsRef().derefComplex();
	}

	/**
	 * Returns oids of objects representing physical references used by the metabase
	 */
	public OID[] getCompiledMetaReferences() throws DatabaseException {
		return getMetaBase().getCompiledMetaReferences();
	}

	/**
	 * Returns the "refnum"th object representing physical reference to a module
	 */
	public OID getCompiledImportAt(int refnum) throws DatabaseException {
		return getCompiledImportsRef().getChildAt(refnum);
	}

	/**
	 * Returns the "refnum"th object representing physical reference used by the metabase
	 */
	public OID getCompiledMetaReferenceAt(int refnum) throws DatabaseException {
		return getMetaBase().getCompiledMetaReferenceAt(refnum);
	}

	/********************************************************************************************************************
	 * operations on database objects
	 */

	public OID createLink(String lname, String host, int port, String schema, String password) throws DatabaseException,
				RDException {
		return LinkManager.getInstance().createLink(lname, this, host, port, schema, password).oid;
	}

	/**
	 * Creates a new view.
	 * 
	 * @param name
	 *           name of the new view
	 * 
	 * @param fldbuf
	 *           estimated number of fields connected to the view (can be 0)
	 */
	public OID createView(String vwname, String voname, byte[] objBody, byte[] binBody, byte[] cnst, byte[] catches)
				throws DatabaseException {
		OID dbv = store.createComplexObject(store.addName(vwname), getDataRef(), DBView.FIELD_COUNT);
		OID vop = store.createComplexObject(store.addName(voname), getDataRef(), DBVirtualObjectsProcedure.FIELD_COUNT);

		new DBView(dbv).initialize(vop);
		new DBVirtualObjectsProcedure(vop).initialize(objBody, binBody, cnst, catches, dbv);

		return dbv;
	}

	public OID createMetaInterface(String intname, String objname, String[] sprint) throws DatabaseException {		
		return getMetaBase().createMetaInterface(intname, objname, sprint);
	}
	
	public OID createDataInterface(String intname, String objname) throws DatabaseException {
		OID intoid = store.createComplexObject(store.addName(intname), getDataRef(), DBInterface.FIELD_COUNT);
		new DBInterface(intoid).initialize(objname);
		
		return intoid;
	}
	
	/**
	 * Creates a new submodule.
	 * 
	 * @param name
	 *           of the submodule
	 */
	public OID createSubmodule(String name) throws DatabaseException {
	    OID modid = store.createComplexObject(store.addName(name), getSubmodulesRef(), DBModule.FIELD_COUNT);

		DBModule mod = new DBModule(modid);

		mod.initialize();
		mod.addImport("system");

		return modid;

	}
	/**
	 * Creates a new submodule.
	 * 
	 * @param name
	 *           of the submodule
	 * @param buf
	 *           buffer for subobjects of the new module
	 *  @deprecated use  public OID createSubmodule(String name) instead
	 */
	public OID createSubmodule(String name, int buf) throws DatabaseException {
		OID modid = store.createComplexObject(store.addName(name), getSubmodulesRef(), buf);

		DBModule mod = new DBModule(modid);

		mod.initialize();
		mod.addImport("system");

		return modid;
	}

	/**
	 * Deletes a submodule.
	 * 
	 * @param OID
	 *           of the submodule
	 */
	public void deleteModule() throws DatabaseException {

		String modname = this.getModuleGlobalName();
		
		// First data stored in module are removed, because they can contain special references e.g. for index update triggers
		this.getDatabaseEntry().delete();
		this.getOID().delete();
		Session.removeModuleFromSession(modname);
		Database.unregisterModule(modname);
	}

	/**
	 * Creates a new runtime procedure.
	 * 
	 * @param name
	 *           name of the procedure
	 * @param objBody
	 *           intermediate code of the procedure (unused)
	 * @param binBody
	 *           binary code of the procedure
	 * @param constants
	 *           constant pool
	 */
	public OID createProcedure(String name, byte[] objBody, byte[] binBody, byte[] constants, byte[] catches) throws DatabaseException {

		if (ConfigDebug.ASSERTS) {
			assert name != null && binBody != null;
		}

		OID procid = store.createComplexObject(store.addName(name), getDataRef(), DBProcedure.FIELD_COUNT);

		DBProcedure prc = new DBProcedure(procid);
		prc.initialize(objBody, binBody, constants, catches);

		return procid;
	}
	
	/**
	 * Creates a new enum.
	 * 
	 * @param name of the enum
	 * @param fieldbuf
	 */
	public OID createEnum(String name, int fieldbuf) throws DatabaseException{
		
		OID enumsid = store.findFirstByNameId(store.addName("$enums"), getDataRef());
		if (enumsid == null)
			enumsid = store.createComplexObject(store.addName("$enums"), getDataRef(),0);
		//getDataRef().createComplexChild(name, children)
		
		OID enuid = store.createComplexObject(store.addName(name), enumsid, 0);
		
		
		
		return enuid;
	}

	/**
	 * Creates a new class with invariant name.
	 * 
	 * @param name
	 *           of the class
	 * @param supernames
	 *           names of the superclasses
	 * @param buf
	 *           buffer for subobjects of the new class
	 * @param invariantName -
	 *           class instance invariant name
	 */
	public OID createClass(String name, int buf, String invariantName) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert name != null;

		OID classid = store.createComplexObject(store.addName(name), getDataRef(), DBClass.FIELD_COUNT);

		DBClass cls = new DBClass(classid);
		cls.initialize(store.addName(invariantName));

		return classid;
	}

	/**
	 * Creates a new class.
	 * 
	 * @param name
	 *           of the class
	 * @param supernames
	 *           names of the superclasses
	 * @param buf
	 *           buffer for subobjects of the new class
	 */
	public OID createClass(String name, int buf) throws DatabaseException {
		if (ConfigDebug.ASSERTS) assert name != null;

		OID classid = store.createComplexObject(store.addName(name), getDataRef(), DBClass.FIELD_COUNT);

		DBClass cls = new DBClass(classid);
		cls.initialize(NO_NAME);

		return classid;
	}

	public OID createLinearHashingIndex(String name, boolean temporary, RecordType recordType, DataAccess dataAccess, String query, byte[] bytecode, byte[] cnstpool) throws DatabaseException {
		OID idxid = store.createComplexObject(store.addName(name), getDataRef(), DBIndex.FIELD_COUNT);
		DBIndex idx = new DBIndex(idxid);
		idx.initialize(IndexStructureKind.LINEARHASHINGMAP_ID, temporary, recordType, dataAccess, query, bytecode, cnstpool);

		return idxid;
	}
	
	/********************************************************************************************************************
	 * operations on metabase objects 
	 * (use MetabaseManager)
	 */

	/**
	 * Creates a new index in the metabase, so that indexes could be used as procedures.
	 * 
	 * @param name
	 *           name of the index
	 * @param varoid
	 *           variable being indexed (must be declared in current or imported module)
	 *          
	 */
	public OID createMetaIndex(String name, boolean temporary, StructSignature sign, RecordType recordType, boolean uniqueNonkeys)
				throws DatabaseException {

		return getMetaBase().createMetaIndex(name, temporary, sign, recordType, uniqueNonkeys);
	}

	/********************************************************************************************************************
	 * module session specific part
	 */

	/**
	 * Creates a new variable object in the session metabase
	 * 
	 * @param name
	 *           name of the variable
	 * @param mincard
	 *           minimum cardinality (see the Bible)
	 * @param maxcard
	 *           maximum cardinality
	 * @param type
	 *           type of the variable
	 * @param ref
	 *           reference indicator (number of &'s in the type specification)
	 *    
	 */
	public OID createSessionMetaVariable(String name, int mincard, int maxcard, String type, int ref)
				throws DatabaseException {
		int typeid = this.addMetaReference(type);

		OID strid = createComplexObject(name, this.getSessionMetaDataRef(), MBVariable.FIELD_COUNT);
		new MBVariable(strid).initialize(typeid, mincard, maxcard, ref);

		return strid;
	}

	/**
	 * Sets new code for module session initialization process
	 * 
	 * @param code -
	 *           the Juliet byte code
	 * @throws DatabaseException
	 */
	public void setSessionInitalizationCode(byte[] code) throws DatabaseException {
		this.getInitRef().updateBinaryObject(code);
	}

	/**
	 * Gets new code for module session initialization process
	 * 
	 * @return - the Juliet byte code for session module initialization
	 * @throws DatabaseException
	 */
	public byte[] getSessionInitalizationCode() throws DatabaseException {
		return this.getInitRef().derefBinary();
	}

	/**
	 * Root of the compile time session content stored in the module's
	 * 
	 * @return pointer of the session metadata entry
	 */
	public OID getSessionMetaDataEntry() throws DatabaseException {
		return this.getSessionMetaDataRef();
	}

	/********************************************************************************************************************
	 * this part is used to operate on SBA objects belonging to modules
	 */

	public OID createIntegerObject(String name, OID parent, int def) throws DatabaseException {
		return store.createIntegerObject(store.addName(name), parent, def);
	}

	public OID createStringObject(String name, OID parent, String def, int buf) throws DatabaseException {
		return store.createStringObject(store.addName(name), parent, def, buf);
	}

	public OID createDoubleObject(String name, OID parent, double def) throws DatabaseException {
		return store.createDoubleObject(store.addName(name), parent, def);
	}

	public OID createBooleanObject(String name, OID parent, boolean def) throws DatabaseException {
		return store.createBooleanObject(store.addName(name), parent, def);
	}

	public OID createComplexObject(String name, OID parent, int buf) throws DatabaseException {
		return store.createComplexObject(store.addName(name), parent, buf);
	}

	public OID createReferenceObject(String name, OID parent, OID def) throws DatabaseException {
		return store.createReferenceObject(store.addName(name), parent, def);
	}

	public OID createPointerObject(String name, OID parent, OID def) throws DatabaseException {
		return store.createPointerObject(store.addName(name), parent, def);
	}

	public OID createAggregateObject(String name, OID parent, int buf) throws DatabaseException {
		return store.createAggregateObject(store.addName(name), parent, buf);
	}
	
	public OID findFirstByName(String name, OID parent) throws DatabaseException {
		return this.store.findFirstByNameId(store.addName(name), parent);
	}

	public OID findFirstByNameId(int nameid, OID parent) throws DatabaseException {
		return this.store.findFirstByNameId(nameid, parent);
	}

	/********************************************************************************************************************
	 * utility methods used to access module fields
	 */

	final OID getKindRef() throws DatabaseException {
		return oid.getChildAt(MOD_OBJKIND_POS);
	}

	final OID getSchemaRef() throws DatabaseException {
		return oid.getChildAt(MOD_SCHEMA_POS);
	}

	final OID getCompiledRef() throws DatabaseException {
		return oid.getChildAt(MOD_COMPILED_POS);
	}

	final OID getLinkedRef() throws DatabaseException {
		return oid.getChildAt(MOD_LINKED_POS);
	}

	final OID getNameRef() throws DatabaseException {
		return oid.getChildAt(MOD_NAME_POS);
	}

	final OID getMetaRef() throws DatabaseException {
		return getMetaBase().getMetabaseEntry();
	}

	final OID getCompiledImportsRef() throws DatabaseException {
		return oid.getChildAt(MOD_COMP_IMPORTS_POS);
	}

	final OID getImportsRef() throws DatabaseException {
		return oid.getChildAt(MOD_IMPORTS_POS);
	}
	
	final OID getImportsAliasesRef() throws DatabaseException {
		return oid.getChildAt(MOD_IMPORTS_ALIASSES_POS);
	}
	
	final OID getImplementsRef() throws DatabaseException {
		return oid.getChildAt(MOD_IMPLEMENTS_POS);
	}
	
	final OID getCompiledMetaReferencesRef() throws DatabaseException {
		return getMetaBase().getCompiledMetaReferencesRef();
	}

	final OID getMetaReferencesRef() throws DatabaseException {
		return getMetaBase().getMetaBaseReferences();
	}

	final OID getDataRef() throws DatabaseException {
		return oid.getChildAt(MOD_DATA_POS);
	}
	
	
	final OID getSubmodulesRef() throws DatabaseException {
		return oid.getChildAt(MOD_SUBMODULES_POS);
	}

	final OID getSessionMetaDataRef() throws DatabaseException {
		return oid.getChildAt(MOD_SESSIONMETADATA_POS);
	}

	final OID getInitRef() throws DatabaseException {
		return oid.getChildAt(MOD_SESSION_INIT_POS);
	}

	public final MetaBase getMetaBase() throws DatabaseException {
		return new MetaBase(oid.getChildAt(MOD_METADATA_POS));
	}

	public long getSerial() throws DatabaseException
	{
		return getMetaBase().getSerial();
	}
	

	/**
	 * Returns if this module is a wrapper module.
	 * 
	 * @return is wrapper?
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public boolean isWrapper() throws DatabaseException {
		return Session.hasWrapperForModule(this);
	}

	/**
	 * Sets a wrapper instance.
	 * 
	 * @param wrapper
	 *           {@link Wrapper}
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public void setWrapper(Wrapper wrapper) throws DatabaseException {
		Session.addWrapper(this, wrapper);
	}

	/**
	 * Returns a wrapper instance.
	 * 
	 * @return {@link Wrapper}
	 * @throws DatabaseException
	 * 
	 * @author jacenty
	 */
	public Wrapper getWrapper() throws DatabaseException {
		return Session.getWrapper(this);
	}
	
	// positions (in lists of children) of objects storing system
	// information about the module
	protected final static int MOD_OBJKIND_POS = 0;
	protected final static int MOD_NAME_POS = 1;
	protected final static int MOD_SCHEMA_POS = 2;
	protected final static int MOD_COMPILED_POS = 3;
	protected final static int MOD_LINKED_POS = 4;
	protected final static int MOD_IMPORTS_POS = 5;
	protected final static int MOD_IMPORTS_ALIASSES_POS = 6;
	protected final static int MOD_IMPLEMENTS_POS =7;
	protected final static int MOD_COMP_IMPORTS_POS = 8;
	protected final static int MOD_METADATA_POS = 9;
	protected final static int MOD_DATA_POS = 10;
	protected final static int MOD_SUBMODULES_POS = 11;
	protected final static int MOD_SESSIONMETADATA_POS = 12;
	protected final static int MOD_SESSION_INIT_POS = 13;
	
	public final static int FIELD_COUNT = 14; 
}
