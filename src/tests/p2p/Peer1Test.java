package tests.p2p;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import odra.cli.CLI;
import odra.cli.batch.BatchException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.Result;
import odra.security.UserContext;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.RemoteP2PStore;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.pu.ClientUnit;

public class Peer1Test {
	
	private ObjectManager manager;
	private static DefaultStore store;

	private Database db;
	public DBInstance instance;
	
	private DBModule gridmodule;
	//private DBLink link;
	
	private void createDatabase() throws Exception {
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("/tmp/test_pu1.dbf");
		fileHeap.format(1024 * 1024 * 20);
		fileHeap.open();

		allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();		
		
		manager = new ObjectManager(allocator);
		manager.initialize(100);
		
		store = new DefaultStore(manager);
		store.initialize();

		// prepare the database
		Database.initialize(store);
		Database.open(store);
		instance = new DBInstance();

		instance.startup();
 
		DBModule sysmod = Database.getSystemModule();
		DBModule mod = Database.getModuleByName("admin");
		
		gridmodule = new DBModule(mod.createSubmodule("grid", 0));


		//mod = p2pmodremote;
		//odra.db.links.LinkManager lman = odra.db.links.LinkManager.getInstance();
		//link = lman.createP2PLink("pu2link", mod, "fullpeer", "admin.pu", "admin");

		
		
		System.out.println("Database created");
	}
	

	public static void main(String[] args){

		//local testing port conflict fix
		odra.virtualnetwork.facade.Config.jxtaTransportPortStart = 9901;
		odra.virtualnetwork.facade.Config.jxtaTransportPortEnd = 9999;
		odra.virtualnetwork.facade.Config.peerPort = 9553;

		odra.virtualnetwork.facade.Config.peerType =odra.virtualnetwork.facade.Config.PEER_TYPE.PEER_ENDPOINT; 

		ConfigServer.LSNR_PORT = 1525;
		ConfigClient.CONNECT_PORT = 1525;
		ConfigServer.WS_SERVER_PORT = 8890;
		ConfigServer.TYPECHECKING = true;

		
		odra.virtualnetwork.facade.Config.repoIdentity = "peer1test";
		odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/odra_pu1");
		
		
		Peer1Test test = new Peer1Test();
		try {
			test.createDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
			
		
		CLI cli = new CLI();
		try {
			cli.execBatch(new String[] {"res/p2p/pu1.cli"});
			cli.begin();
		} catch (BatchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
		
}

