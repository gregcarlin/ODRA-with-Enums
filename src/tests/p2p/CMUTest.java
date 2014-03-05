package tests.p2p;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axis2.transport.http.AdminAgent;

import odra.OdraCoreAssemblyInfo;
import odra.cli.batch.BatchException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.virtualnetwork.CMUHandlerImpl;
import odra.virtualnetwork.RequestHandlerImpl;
import odra.virtualnetwork.api.ExternalConsole;
import odra.virtualnetwork.api.LocalTransport;
import odra.virtualnetwork.api.TransportPeer;
import odra.virtualnetwork.api.WindowHandler;
import odra.virtualnetwork.cmu.CMUnit;



public class CMUTest{
	CMUnit cmu = null;
	
	private ObjectManager manager;
	private DefaultStore store;
	private DBInstance instance;
	private DBModule admin_module;
	private static SocketChannel sc;
	
	private WindowHandler handler = null;
	private Logger logger = null;

	
	public void createDatabase() throws Exception{
		try
		{
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("/tmp/test_cmu.dbf");
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
 
//		DBModule sysmod = Database.getSystemModule();
	//	admin_module = Database.getModuleByName("admin");
		
		System.out.println("Database created");

		}
		catch (DatabaseException exc)
		{
			exc.printStackTrace();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
		CMUTest test = new CMUTest();
		
//		test.handler = WindowHandler.getInstance();
	    //obtaining a logger instance and setting the handler
//		test.logger = Logger.getLogger("CMUnit");
//	    test.logger.addHandler(test.handler);

		odra.virtualnetwork.facade.Config.peerType  = odra.virtualnetwork.facade.Config.PEER_TYPE.PEER_CMU;
		odra.virtualnetwork.facade.Config.peerPort = 9554;
		odra.virtualnetwork.facade.Config.peerMonitor = false;
		odra.virtualnetwork.facade.Config.repoIdentity = "cmu";
		odra.virtualnetwork.facade.Config.platformHome = URI.create("file:///tmp/odra_cmu");
		
		ConfigServer.LSNR_PORT = 1523;
		ConfigServer.TYPECHECKING = true;
		ConfigClient.CONNECT_PORT = 1523;
		

		try {
			test.createDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		odra.cli.CLI cli = new odra.cli.CLI();
		try {
			cli.execBatch(new String[] {"res/p2p/cmu.cli"});
			cli.begin();
		} catch (BatchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//turn off autoconnect beacuse CLI Class is used in ServerProcces
		ConfigClient.CONNECT_AUTO = false;
	}
}
