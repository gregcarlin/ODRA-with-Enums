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
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.CMUHandlerImpl;
import odra.virtualnetwork.RemoteP2PStore;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.facade.Config;
import odra.virtualnetwork.cmu.CMUnit;
import odra.virtualnetwork.pu.ClientUnit;

/**
 *Startuje bazę danych i tworzy w niej DBPeer (analog DBLink)
 *za pomocą tego obiektu dokonuje remoteBind i deref 
 */
public class Peer2Test {
	private ObjectManager manager;
	private static DefaultStore store;

	private Database db;
	private DBInstance instance;
	
	private DBModule gridmodule;
	//private OID xoid;
	
	
	
	private void createDatabase() throws Exception {
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("/tmp/test_pu2.dbf");
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
		
		//mod.createMetaVariable("x", 1, 1, "integer", 0);
		//mod.createIntegerObject("x", mod.getDatabaseEntry(), 27);
		
		
		gridmodule = new DBModule(mod.createSubmodule("grid", 0));

		/*
		mod = p2pmodremote;
		
		p2pmodremote.createMetaVariable("x", 1, 1, "integer", 0);
		p2pmodremote.createIntegerObject("x", mod.getDatabaseEntry(), 27);
		p2pmodremote.createMetaVariable("str", 1, 1, "string", 0);
		p2pmodremote.createStringObject("str", mod.getDatabaseEntry(), "string !!", 30);
		p2pmodremote.createMetaVariable("y", 1, 1, "real", 0);
		p2pmodremote.createDoubleObject("y", mod.getDatabaseEntry(),12.5);
		
		p2pmodremote.createMetaVariable("b", 1, 1, "boolean", 0);
		p2pmodremote.createBooleanObject("b", mod.getDatabaseEntry(), false);
		
		//mod.createMetaVariable("com", 1, 1, "complex", 0);
		OID complex = mod.createComplexObject("com", mod.getDatabaseEntry(), 0);
		mod.createStringObject("nazwa", complex, "jakas nazwa", 0);
		mod.createIntegerObject("numer", complex, 12);
		
		OID empagg = mod.createAggregateObject("emp", mod.getDatabaseEntry(), 0);

		for (int i = 0; i < 5; i++) {
		OID e1 = mod.createComplexObject("emp", empagg, 0);
		mod.createStringObject("first", e1, "x", 0);
		OID a1 = mod.createComplexObject("address", e1, 0);
		mod.createStringObject("street", a1, "krotka", 0);
		mod.createStringObject("town", a1, "warszawa", 0);

		OID e2 = mod.createComplexObject("emp", empagg, 0);
		mod.createStringObject("first", e2, "x", 0);
		OID a2 = mod.createComplexObject("address", e2, 0);
		mod.createStringObject("street", a2, "krotka", 0);
		mod.createStringObject("town", a2, "warszawa", 0);			

		OID e3 = mod.createComplexObject("emp", empagg, 0);
		mod.createStringObject("first", e3, "x", 0);
		OID a3 = mod.createComplexObject("address", e3, 0);
		mod.createStringObject("street", a3, "krotka", 0);
		mod.createStringObject("town", a3, "warszawa", 0);		
		
		} 
		*/
		
		System.out.println("Database created");
	}

	public static void main(String[] args){

		//local testing port conflict fix
		odra.virtualnetwork.facade.Config.jxtaTransportPortStart = 9801;
		odra.virtualnetwork.facade.Config.jxtaTransportPortEnd = 9899;
		odra.virtualnetwork.facade.Config.peerPort = 9551;
		
		odra.virtualnetwork.facade.Config.peerType =odra.virtualnetwork.facade.Config.PEER_TYPE.PEER_ENDPOINT; 
		
		ConfigServer.LSNR_PORT = 1521;
		ConfigServer.WS_SERVER_PORT = 8889;
		ConfigServer.TYPECHECKING = true;
		
		odra.virtualnetwork.facade.Config.repoIdentity = "peer2test";
		odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/odra_pu2");
		

		Peer2Test test = new Peer2Test();
		try {
			test.createDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	
		CLI cli = new CLI();
		try {
			cli.execBatch(new String[] {"res/p2p/pu2.cli"});
			cli.begin();
		} catch (BatchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
