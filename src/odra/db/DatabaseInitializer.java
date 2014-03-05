package odra.db;

import java.util.logging.Level;

import odra.db.indices.IndexManager;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBSystemModule;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ParserException;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.CompilerException;
import odra.sbql.builder.LinkerException;
import odra.sbql.builder.ModuleConstructor;
import odra.sbql.builder.OrganizerException;
import odra.sbql.typechecker.TypeCheckerException;
import odra.system.Names;
import odra.system.config.ConfigServer;
import odra.ws.facade.IEndpointFacade;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;

/**
 * This class is used to initialize newly created databases.
 * It does two things:
 * 1. creates the system module
 * 2. creates the data dictionary
 *
 * @author raist
 */

public class DatabaseInitializer {
	private static IDataStore store = Database.getStore();

	/**
	 * Creates the system module in the database.
	 * The module is used as a root of the tree of modules.
	 * Its submodules are user-schema modules.
	 */
	public final static DBSystemModule createSystemModule() throws DatabaseException {
		OID modid = store.createComplexObject(store.addName("system"), store.getEntry(), 10);

		DBSystemModule sysmod = new DBSystemModule(modid);
		sysmod.initialize();
		addSystemModuleField(sysmod, "class Exception {  instance : {message:string;}	  getMessage():string {return message; } setMessage(msg:string) {message := msg; }}");
		//addSystemModuleField(sysmod, "type OID is record {URL:string; SCHEMA:string; ID:string;}");
//		addSystemModuleField(sysmod, "distinct type OID is string;");
		return sysmod;
	}

	/**
	 * Creates the data dictionary.
	 * TODO: read scripts from *.sbql files.
	 */
	public final static void createMetadata() throws DatabaseException, LinkerException, OrganizerException, CompilerException, TypeCheckerException {
		String adminModtxt = "module " + Database.ADMIN_SCHEMA + "{ }";
		String wsModtxt = "module " + Database.WS_SCHEMA + "{ }";

		linkModule(Database.SYSTEM_SCHEMA);
		compileModule(Database.SYSTEM_SCHEMA);

		buildModule(Database.SYSTEM_SCHEMA, adminModtxt);
		linkModule(Database.ADMIN_SCHEMA);
		compileModule(Database.ADMIN_SCHEMA);
		
		if (!Database.WS_SCHEMA.equals(Database.ADMIN_SCHEMA)) {
			buildModule(Database.SYSTEM_SCHEMA, wsModtxt);
			linkModule(Database.WS_SCHEMA);
			compileModule(Database.WS_SCHEMA);
		}

		DBModule admin = Database.getModuleByName(Database.ADMIN_SCHEMA);
		DBModule ws = Database.getModuleByName(Database.WS_SCHEMA);

		// endpoints
		if (ConfigServer.WS) {
			IEndpointFacade endpointsManager = WSManagersFactory.createEndpointManager();
			IProxyFacade proxiesManager = WSManagersFactory.createProxyManager();
		
			if (endpointsManager == null) {
				throw new DatabaseException("Error while creating endpoints metadata. Driver is missing. ");
				
			}
			if (proxiesManager == null) {
				throw new DatabaseException("Error while creating proxies metada. Driver is missing. ");
				
			}
			
			endpointsManager.createMetadata(ws);
			proxiesManager.createMetadata(ws);
		}
		
		IndexManager.initialize(admin, admin.createAggregateObject(Names.namesstr[Names.S_SYSINDICES_ID], admin.getDatabaseEntry(), 0));
		
		admin.createAggregateObject(Names.namesstr[Names.S_SYSUSERS_ID], admin.getDatabaseEntry(), 0);
		admin.createAggregateObject(Names.namesstr[Names.S_PRVLINKS_ID], admin.getDatabaseEntry(), 0);
		admin.createAggregateObject(Names.namesstr[Names.S_SYSROLES_ID], admin.getDatabaseEntry(), 0); 
		// TODO: ^^ move it to the part which will be responsible for creating user accounts
	}

	/**
	 * Builds a module with the source code given as a parameter.
	 * @param parent name of the parent module
	 * @param src source code of the module
	 */
	private final static void buildModule(String parent, String src) throws DatabaseException, OrganizerException {
		try {
			ASTNode ast = BuilderUtils.parseSBQL(src);

			ModuleConstructor con = new ModuleConstructor(Database.getModuleByName(parent));
			ast.accept(con, null);
		}
		catch (OrganizerException ex) {
			throw ex;
		}
		catch (ParserException ex) {
			throw ex;
		}
		catch (Exception ex) {
			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during module building process", ex);
		}
	}

	/**
	 * Finds a module with the name given as a paramter and links it
	 * @param module name of the module
	 */
	private final static void linkModule(String module) throws DatabaseException, LinkerException {
		BuilderUtils.getModuleLinker().linkModule(Database.getModuleByName(module));
	}

	/**
	 * Finds a module with the name given as a parameter and compiles it
	 * @param module name of the module
	 */
	private final static void compileModule(String module) throws DatabaseException, CompilerException, TypeCheckerException {
		BuilderUtils.getModuleCompiler().compileModule(Database.getModuleByName(module));
	}

	/**
	 * Finds a module with the name given as a parameter and checks it typologically
	 * @param module name of the module
	 */
	private final static void checkModule(String module) throws DatabaseException {
	}
	
	private final static void addSystemModuleField(DBModule mod, String classDeclarationSrc){
	    
	    
	    try {
		ASTNode ast = BuilderUtils.parseSBQL(classDeclarationSrc);

		ModuleConstructor modConstructor = new ModuleConstructor(null, true,false);
		    modConstructor.setConstructedModule(mod);
		    ast.accept(modConstructor, null);
        	}
        	catch (OrganizerException ex) {
        		throw ex;
        	}
        	catch (ParserException ex) {
        		throw ex;
        	}
        	catch (Exception ex) {
        		ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during module building process", ex);
        	}
	    
	}
}
