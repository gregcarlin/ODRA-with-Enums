package odra.db;

import odra.db.IDataStoreExtension.ExtensionType;
import odra.db.objects.data.DBModule;
import odra.db.objects.data.DBSystemModule;
import odra.db.objects.meta.MBLink;
import odra.security.AccountManager;
import odra.security.RoleManager;
import odra.store.DefaultStore;
import odra.store.sbastore.NameIndex;
import odra.system.Names;
import odra.system.config.ConfigServer;
import odra.transactions.store.IDataStoreTransactionExtension;
import odra.virtualnetwork.facade.ICMUUnit;
import odra.virtualnetwork.facade.IPeerUnit;
import odra.virtualnetwork.facade.VirtualNetworkFactory;
import odra.ws.facade.IEndpointFacade;
import odra.ws.facade.IProxyFacade;
import odra.ws.facade.WSManagersFactory;

/**
 * This class represents a jodra database.
 * 
 * @author raist
 */

public final class Database {
	private static IDataStore store;

	private static DBSystemModule sysmod;

	private static NameIndex nidx;

	private static ModuleFinder finder = new ModuleFinder();

	private Database() {
	}

	public static NameIndex getNameIndex() {
		return nidx;
	}

	public static void setNameIndex(NameIndex nidx) {
		Database.nidx = nidx;
	}

	/**
	 * Returns the global datastore of this database
	 */
	public static IDataStore getStore() {
		return store;
	}

	/**
	 * Opens the database
	 */
	public static void open(IDataStore s) throws Exception {
		store = s;
		sysmod = new DBSystemModule(store.getRoot());
		nidx = ((DefaultStore) s).getNameIndex();

		StdEnvironment.init(sysmod);

		wsInitialize();
		gridInitialize();
	}

	private static void wsInitialize() throws DatabaseException {
		if (ConfigServer.WS) {
			IEndpointFacade endpointFacade = WSManagersFactory.createEndpointManager();
			IProxyFacade proxyFacade = WSManagersFactory.createProxyManager();

			if (endpointFacade == null) {
				throw new DatabaseException("Cannot find endpoint driver. ");
			}
			if (proxyFacade == null) {
				throw new DatabaseException("Cannot find proxy driver. ");
			}

			endpointFacade.initialize();
			proxyFacade.initialize();

		}
	}
	
	private static void gridInitialize() {
	    if (ConfigServer.JXTA) {
	    	switch (odra.virtualnetwork.facade.Config.peerType){
	    	case PEER_CMU: 
	    		ICMUUnit cmu = VirtualNetworkFactory.createCMUUnit();
	    		cmu.setCMUHandler(VirtualNetworkFactory.createCMUHandler());
	    		VirtualNetworkFactory.createRequestHandler();
	    		cmu.initialize();			
	    		break;
	    	case PEER_ENDPOINT:				
	    		IPeerUnit peer = VirtualNetworkFactory.createPeerUnit();
	    		VirtualNetworkFactory.createRequestHandler();
			
	    		peer.startLight();
	    		peer.start();
	    		break;
	    	}
	    }
	}

	/**
	 * Initializes the datastore so that it could be used as an odra database. It creates the module system (which stores
	 * mainly the metabase being being standard environment for sbql) and a system user account (which stores the data
	 * dictionary and other system-level data).
	 */
	public static void initialize(IDataStore s) throws Exception {
		store = s;
		// nidx = new NameIndex(s.getChildAt(s.getEntry(), 0));

		sysmod = DatabaseInitializer.createSystemModule();

		StdEnvironment.init(sysmod);

		DatabaseInitializer.createMetadata();

		// crete MBLink for localhost with "$localhostlink" name
		OID linkid = sysmod.createComplexObject(Names.namesstr[Names.LOCALHOST_LINK], sysmod.getMetabaseEntry(),
					MBLink.FIELD_COUNT);
		new MBLink(linkid).initialize("localhost", 0, "");

		RoleManager.registerSystemRole("admin");
		AccountManager.registerUserAccount(ConfigServer.WS_CONTEXT_USER, ConfigServer.WS_CONTEXT_USER);
		AccountManager.registerUserAccount("admin", "admin");
	}

	/**
	 * Finds a module having a particular name.
	 * 
	 * @param global
	 *           name of a module (e.g. ala.ma.kota).
	 */
	public static DBModule getModuleByName(String modpath) throws DatabaseException {
		return finder.getModuleByName(modpath);
	}
	
	/**
	 * Returns the system module. The system module stores mainly the standard environment. Its submodules are user
	 * account modules.
	 */
	public static DBSystemModule getSystemModule() throws DatabaseException {
		return sysmod;
	}
	public static void unregisterModule(String name){
	    finder.unregisterModule(name);
	}
	// ------------------------------------------------

	// Names of some system objects
	public final static String SYSTEM_SCHEMA = "system";

	public final static String ADMIN_SCHEMA = "admin";

	public final static String WS_SCHEMA = ConfigServer.WS_CONTEXT_USER;

	private static void initializeTransactions() {
		if (store.hasExtension(ExtensionType.TRANSACTIONS)) {
			IDataStoreTransactionExtension extTrans = store.getTransactionExtension();
		}
	}
}