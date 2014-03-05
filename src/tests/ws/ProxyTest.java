package tests.ws;

import static org.junit.Assert.fail;

import java.io.IOException;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.exceptions.rd.RDException;
import odra.sessions.Session;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Proxy testing tool
 * 
 * @since 2007-04-24
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */
public class ProxyTest extends CLI {
	private static DBModule sysmod;

	private static DBModule admod;

	private static DBModule mod;

	private OID objectsContainer;

	private static DefaultStore store;

	private static DBInstance instance;

	private static int size = 1 * 1024 * 1024;

	
	private static String dbfFilePath = "wsdl_test.db";

	@BeforeClass
	public static void setUp() throws Exception {
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
		OID sub = admod.getSubmodule("ProxyTests");
		if (sub != null) {
			sub.delete();
		}
	
		mod = new DBModule(admod.createSubmodule("ProxyTests", 0));
		
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
		prepareModules();
		try {
			this.begin();

		} catch (Exception ex) {
			fail();
		}
	}

	private void prepareModules() {
		try {
			this.execCm(new String[] { "ProxyTests" });

			this.execAddModuleAsProxy(new String[] { "ReverseProxy",
					"http://localhost:7777/WSExample/ReverseService.asmx?WSDL" },
					"admin.ProxyTests");
			this.execAddModuleAsProxy(new String[] { "ComplexProxy",
					"http://localhost:7777/WSExample/ComplexService.asmx?WSDL" },
					"admin.ProxyTests");
			this.execAddModuleAsProxy(new String[] { "StocksProxy", 
					"http://localhost:7777/WSExample/StockService.asmx?WSDL"},
					"admin.ProxyTests");
		
		} catch (RDException ex) {
			Assert.fail("Internal problem with databse. ");

		} catch (IOException ex) {
			Assert.fail("Internal problem with databse. ");

		}
	}

	
}
