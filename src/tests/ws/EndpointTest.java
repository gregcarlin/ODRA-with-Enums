package tests.ws;

import static org.junit.Assert.fail;
import odra.cli.CLI;
import odra.db.Database;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.sessions.Session;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/** Endpoint testing tool
 * 
 * @since 2007-04-24
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class EndpointTest extends CLI{
	private static DBModule sysmod;
	private static DBModule admod;
	private static DBModule mod;
	private OID objectsContainer;
		
	private static DefaultStore store;
	private static DBInstance instance;
	private static int size = 1 * 1024 * 1024;
	
	private static String dbfFilePath = "wsdl_test.db";
	
	@BeforeClass
	public static void setUp() throws Exception
	{
		DataFileHeap fileHeap = new DataFileHeap(dbfFilePath);
		fileHeap.format(size);
		fileHeap.open();

		RevSeqFitMemManager allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();
		
		ObjectManager manager = new ObjectManager(allocator);
		manager.initialize(100);

		store = new DefaultStore(manager);
		store.initialize();

		Database.initialize(store);
		Database.open(store);
		
		instance = new DBInstance();
		instance.startup();
		
		sysmod = Database.getSystemModule();
		
				
 		admod = new DBModule(sysmod.getSubmodule("admin"));
 		OID sub = admod.getSubmodule("EndpointTests");
 		if (sub != null) {
 			sub.delete();
 		}
 		
 		mod = new DBModule(admod.createSubmodule("EndpointTests", 0));
	
 		Session.create();
 		Session.initialize("admin", "admin");
 		
 				
	}
	
	@AfterClass
	public static void tearDown() {

		Session.close();
		
		if (instance != null) {
			instance.shutdown();
		}
		if (store != null) {
			store.close();
		}
		
	}
	
	@Test 
	public void Env() {
 		try {
 		 	this.execCm(new String[] { "EndpointTests" });
 		
 		 	this.execBatch(new String[] {"res\\ws\\endpoint\\simple.cli"});
 		 	this.execBatch(new String[] {"res\\ws\\endpoint\\math.cli"});
 		 	this.execBatch(new String[] {"res\\ws\\endpoint\\faculty.cli"});
 		 	this.execBatch(new String[] {"res\\ws\\endpoint\\auction.cli"});
 		 	
 			System.out.flush();
 			
 			this.begin();
 			
 			
 		} catch (Exception ex) {
 			fail();
 		}
	}
	
}
