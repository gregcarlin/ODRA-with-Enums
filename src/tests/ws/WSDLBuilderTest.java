package tests.ws;

import java.io.IOException;

import odra.cli.CLI;
import odra.cli.batch.BatchException;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBClass;
import odra.db.objects.meta.MBObject;
import odra.db.objects.meta.MBProcedure;
import odra.dbinstance.DBInstance;
import odra.exceptions.rd.RDException;
import odra.security.AuthenticationException;
import odra.sessions.Session;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.ws.bindings.BindingsHelper;
import odra.ws.endpoints.EndpointState;
import odra.ws.endpoints.WSEndpointOptions;
import odra.ws.endpoints.wsdl.ClassWSDLBuilder;
import odra.ws.endpoints.wsdl.ProcWSDLBuilder;
import odra.ws.endpoints.wsdl.WSDLBuilderException;
import odra.ws.facade.Config;
import odra.ws.type.mappers.literal.LiteralTypeMapper;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests WSDL contract creation
 * 
 * @since 2007-04-24
 * @version 2007-06-24
 * @author Marcin Daczkowski <merdacz@mat.uni.torun.pl>
 *
 */

public class WSDLBuilderTest extends CLI {
	private static DBModule sysmod;
	private static DBModule admod;
	private static DBModule mod;
	private OID objectsContainer;
	
	private static DefaultStore store;
	private static DBInstance instance;
	private static int size = 1 * 1024 * 1024;
	
	private static String dbfFilePath = "wsdl_test.dbf";

	
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
 		OID sub = admod.getSubmodule("WSDLTests");
 		if (sub != null) {
 			sub.delete();
 		}
 		
 		mod = new DBModule(admod.createSubmodule("WSDLTests", 0));
		
 		
 		
		
	}
	
	@AfterClass
	public static void tearDown() {
	
		if (instance != null) {
			instance.shutdown();
		}
		if (store != null) {
			store.close();
		}
	}
	
	@Test
	public void Session() throws DatabaseException, AuthenticationException
	{		
		for (int i=0; i<200; i++) 
		{
			Session.create();
	 		Session.initialize("admin", "admin");
			Session.close();
		}
		
	}
	

}
