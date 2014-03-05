package odra.db.indices;

import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.IDataStore;
import odra.db.OID;
import odra.db.indices.dataaccess.DataAccess;
import odra.db.indices.dataaccess.OIDAccess;
import odra.db.indices.keytypes.IndexUniqueNKKeyType;
import odra.db.indices.recordtypes.RecordType;
import odra.db.indices.recordtypes.SimpleRecordType;
import odra.db.indices.structures.IndexStructureKind;
import odra.db.indices.structures.LinearHashingMap;
import odra.db.indices.updating.IndexableStore;
import odra.db.objects.data.DBIndex;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBIndex;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleCompiler;
import odra.sbql.builder.ModuleLinker;
import odra.sbql.builder.ModuleOrganizer;
import odra.sbql.emiter.IJulietCodeGenerator;
import odra.system.Names;
import odra.system.config.ConfigServer;

/**
 * Index Register class.
 * <br>
 * Index Register is located inside admin module in a root aggregated object 
 * named $sysindices<br>
 * <br>
 * Index Register structure :
 * <ul>
 * <li>indexlist (list of all created indices)
 * <ul>
 * <li>MBIndex#1 oid (name of this reference object is "module.indexname")</li>
 * <li>MBIndex#2 oid </li>
 * <li>.....</li>
 * </ul></li>
 * <li>nonkeylist (list of all nonkey value objects occuring in indices)
 * <ul>
 * <li>uniquenonkey#1 (information concerning unique nonkey value AST and objects varref)</li>
 * <li>uniquenonkey#2</li>
 * <li>.....</li>
 * </ul></li>      
 * <li>nonkeyindex (list of uniquenonkeys indexed by AST and objects varref)</li>
 * </ul>
 * <br>
 * @author tkowals, raist
 * @version 1.0
 */

public class IndexManager {
	private ModuleLinker linker;
	private ModuleCompiler compiler;
	protected IJulietCodeGenerator generator;

	private DBModule admod;
	private OID oid;
	
	private Index nkidx;
	
	private static final String idx_name_prefix = "$index_";
	
	/**
	 * Constructor used for managing indices
	 * @param linker odra module linker
	 * @param compiler odra module compiler
	 * @param checker odra SBQL typechecker
	 * @param generator obra Juliet bytecode generator
	 * @throws DatabaseException
	 */
	public IndexManager(ModuleLinker linker, ModuleCompiler compiler,  IJulietCodeGenerator generator) throws DatabaseException {
		this(); 
		
		this.linker = linker;
		this.compiler = compiler;
		this.generator = generator;
	}

	/**
	 * Constructor used by index optimizer - managing indices is not necessary
	 * @throws DatabaseException
	 */
	public IndexManager() throws DatabaseException {

		admod = Database.getModuleByName("admin");

		oid = admod.findFirstByNameId(Names.S_SYSINDICES_ID, admod.getDatabaseEntry());

		if (oid == null)
			throw new DatabaseException("Invalid database configuration");

		RecordType recordType = new SimpleRecordType(getRecordTypeRef());
		DataAccess dataAccess = new OIDAccess(getDataAccessRef());
		nkidx = IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID, getNonkeyIndexRef().getChildAt(INDEXOID_POS), recordType, dataAccess);		
	}
	
	/**
	 * Initializes the index register in the database by creating some system-level subobjects. 
	 * @param admod admin module
	 * @param oid complex object for index register
	 * @throws DatabaseException
	 */
	public static void initialize(DBModule admod, OID oid) throws DatabaseException {
		admod.createComplexObject("$indexlist", oid, 0);
		admod.createComplexObject("$nonkeylist", oid, 0);
		OID nkidxoid = admod.createComplexObject("$nonkeyindex", oid, 0);
		
		RecordType recordType = new SimpleRecordType(new IndexUniqueNKKeyType());
		recordType.initialize(admod.createComplexObject(Names.namesstr[Names.RECORDTYPE_ID], nkidxoid, 1));
		DataAccess dataAccess = new OIDAccess();
		dataAccess.initialize(admod.createComplexObject(Names.namesstr[Names.DATAACCESS_ID], nkidxoid, 1)); 		
		
		LinearHashingMap nidxmap = (LinearHashingMap) IndexStructureKind.generateIndex(IndexStructureKind.LINEARHASHINGMAP_ID, admod.createComplexObject("$index", nkidxoid, LinearHashingMap.FIELDS_COUNT), recordType, dataAccess); 
		nidxmap.initialize(31, 3, 75, 65);
		
	}
	
	/**
	 * Creates and registers new index in the database
	 * @param idxname index name
	 * @param temporary true for temporary, false for materialized index
	 * @param rParams parameters indicating record types for each key (dense, range or enum)
	 * @param prog creating query
	 * @param modname name of module where index will be created
	 * @throws Exception
	 */
	public void createIndex(String idxname, boolean temporary, String[] rParams, String prog, String modname) throws Exception {

		if (getIndexOID(modname+"."+idxname) != null)
			throw new IndicesException("Index " + idxname + " already exist in " + modname + ".", null);
		
		DBModule mod = Database.getModuleByName(modname);		
		if ((prog.indexOf("(") != -1) && ((prog.indexOf("join") == -1) || (prog.indexOf("(") < prog.indexOf("join"))))
			prog = prog.substring(0, prog.indexOf("(")) + " join " + prog.substring(prog.indexOf("("));
		if (prog.charAt(prog.length() - 1) == ';')
			prog = prog.substring(0, prog.length() - 1);
		
		ASTNode node = BuilderUtils.parseSBQL(prog);

		linker.linkModule(mod);
		compiler.compileModule(mod);

		if (!(node instanceof JoinExpression))
			throw new IndicesException("Creating query should be based on join operator", null);
				
		if (ConfigServer.TYPECHECKING) {
			OID mbidxoid = SafeGenerateIndex.createIndex(this, node, mod, idx_name_prefix + idxname, temporary, prog, rParams);
			registerIndex(modname+"."+idxname, mbidxoid);
		} else {
			if (temporary)
				throw new IndicesException("Temporary indices are disabled in dynamic mode", null);

			// TODO: estimate if index in dynamic mode can be used 			
			OID idxoid = DynamicGenerateIndex.createIndex(this, node, mod, idxname, prog, rParams);
			admod.createReferenceObject(modname+"."+idxname, getIndexList(), idxoid);
		}					
		
	}

	private void registerIndex(String idxname, OID mbidxoid) throws DatabaseException {
		admod.createReferenceObject(idxname, getIndexList(), mbidxoid);
		MBIndex mbidx = new MBIndex(mbidxoid);

		NonkeyIndexRegister nkidxreg;
		OID unkoid = lookupNonkeyRef(mbidx.getNonKeyASTText(), mbidx.getIdxVar());
		if (unkoid == ASTNOTFOUND) {
			unkoid = admod.createComplexObject("$uniquenonkey", getNonkeyListRef(), 3);
			nkidxreg = new NonkeyIndexRegister(unkoid, admod);
			nkidxreg.initialize(mbidx);
			
			nkidx.insertItem(unkoid, unkoid); 			
		} else 		
			nkidxreg = new NonkeyIndexRegister(unkoid, admod);
		
		nkidxreg.registerKeys(mbidxoid);
		
	}
	
	/**
	 * Removes and unregisters an index
	 * @param idxname name of index
	 * @param modname index parent module 
	 * @throws Exception 
	 */
	public void removeIndex(String idxname, String modname) throws Exception {
		
		OID idxoid = getIndexOID(modname+"."+idxname);
		
		if (idxoid == null)
			throw new IndicesException("Index " + idxname + " does not exist in " + modname + ".", null);
		
		ModuleOrganizer mo = new ModuleOrganizer(Database.getModuleByName(modname), false);		
		
		if (ConfigServer.TYPECHECKING) {
			OID mbidxoid = idxoid.derefReference();
			unregisterIndex(mbidxoid);
			mo.deleteMetabaseObject(new MBIndex(mbidxoid)); //removes also all references in Index Manager to this MBIndex
		}

		IDataStore store = Database.getStore(); 
		if (store instanceof IndexableStore) {
			DBModule mod = Database.getModuleByName(modname); 
			DBIndex dbidx = new DBIndex(store.findFirstByNameId(store.getNameId(idx_name_prefix + idxname),
					mod.getDatabaseEntry()));
			if (!dbidx.isTemporary())
				dbidx.getTriggersManager(mod).disableAutomaticUpdating();
			else dbidx.removeTemporaryIndex();		
		}
		
		mo.deleteDatabaseObject(idx_name_prefix + idxname); //removes also all references in Index Manager to this DBIndex  	
	}
	
	private void unregisterIndex(OID mbidxoid) throws DatabaseException {
		MBIndex mbidx = new MBIndex(mbidxoid);
		
		OID unkoid = lookupNonkeyRef(mbidx.getNonKeyASTText(), mbidx.getIdxVar());
		if (NonkeyIndexRegister.countUNKIndices(unkoid) == 1) {

			if (!nkidx.removeItem(unkoid, unkoid))
				throw new DatabaseException("Index of unique nonkeys is damaged");

			unkoid.delete();
			return;			
		} else {
			NonkeyIndexRegister nkidxreg = new NonkeyIndexRegister(unkoid, admod);
			nkidxreg.unregisterKey(mbidxoid);
		}		

	}
	
	private OID getIndexOID(String idxname) throws DatabaseException {				
		int idxnameid = Database.getStore().getNameId(idxname);
		
		return admod.findFirstByNameId(idxnameid, getIndexList());
	}		

	/**
	 * @param ASTText textually serialized AST of index nonkey 
	 * @param varref MBVariable describing nonkey
	 * @return nonkeyindex register object OID for given parameters
	 * @throws DatabaseException
	 */
	public final OID lookupNonkeyRef(String ASTText, OID varref)  throws DatabaseException {
		return (OID) nkidx.lookupItem(new Object[] {ASTText, varref});
	}
	
	/***********************************
	 * access to general subobjects describing the IndexManager state
	 * */ 
	
/*	private final int countUniqueNonkeys() throws DatabaseException {
		return getNonkeyListRef().countChildren();
	}

	private final Index getNonkeyIndex() throws DatabaseException {
		return nkidx;
	}	
	
	private final OID getUniqueNonkeyRef(int nonkeynum) throws DatabaseException {
		return getNonkeyListRef().getChildAt(nonkeynum);
	}*/
	
	private OID getIndexList() throws DatabaseException {		
		return oid.getChildAt(INDEXLIST_POS);
	}
	
	private final OID getNonkeyListRef() throws DatabaseException {
		return oid.getChildAt(NONKEYLIST_POS);
	}	

	private final OID getRecordTypeRef() throws DatabaseException {
		return getNonkeyIndexRef().getChildAt(RECORDTYPE_POS);
	}
	
	private final OID getDataAccessRef() throws DatabaseException {
		return getNonkeyIndexRef().getChildAt(DATAACCESS_POS);
	}
	
	private final OID getNonkeyIndexRef() throws DatabaseException {
		return oid.getChildAt(NONKEYINDEX_POS);
	}	
	
	private static final int INDEXLIST_POS = 0;
	private static final int NONKEYLIST_POS = 1;
	private static final int NONKEYINDEX_POS = 2;
	
	// nonkeyindex subfields
	private final static int RECORDTYPE_POS = 0;
	private final static int DATAACCESS_POS = 1;
	private final static int INDEXOID_POS = 2;
	
	public final static OID ASTNOTFOUND = null;
	
}
