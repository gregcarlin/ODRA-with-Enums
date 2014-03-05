package tests;

import java.net.UnknownHostException;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.OID;
import odra.db.objects.data.DBModule;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigDebug;

/**
 * Jodra main class
 * 
 * @author raist
 */

public class MLMain //implements DataImporter 
{
	private ObjectManager manager;
	private DefaultStore store;

	private Database db;
	private DBInstance instance;

	private void createDatabase() throws Exception {
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("c:\\test.dbf");
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
 
	
		DBModule sysmod = Database.getSystemModule();
		DBModule mod = Database.getModuleByName("admin");
		DBModule newmod = new DBModule(mod.createSubmodule("test", 0));

		
		
/*		newmod.createMetaVariable("x", 1, 1, "integer", 0);
		
		MBStruct str = new MBStruct(newmod.createMetaStruct(0));
		str.createField("a", 1, 1, "integer", 0);
		str.createField("b", 1, 1, "string", 0);
		str.createField("d", 1, 1, "string", 0);
*/

//		newmod.createMetaVariable("y", 1, 1, str.getName(), 0);		

//		OID id = newmod.createMetaAnnotatedVariable("mao", 1, 1, "integer", 0);
//		MBAnnotatedVariableObject mb = new MBAnnotatedVariableObject(id);
//		mb.createAnnotation("zzz", 1, 1, "integer", 0);
//		mb.createAnnotation("www", 1, 1, "integer", 0);

/*		MBView mbv = new MBView(newmod.createMetaView("testview", "empdept", "string", 1, 1, 0));
		mbv.createVariableField("cos", 1, 1, "string", 0);
		mbv.createSubview("sbview", "sbobj", "integer", 1, 1, 0);
		mbv.createProcedureField("proc", 1, 1, "string", 0, 0);

		DBView dbv = new DBView(newmod.createView("testview", "empdept", new byte[0], new byte[0], new byte[0], new byte[0]));
		dbv.createView("sbview", "sbobj", new byte[0], new byte[0], new byte[0], new byte[0]);

		dbv.createProcedureField("proc", new byte[0], new byte[0], new byte[0], new byte[0]);
*/
		
// 		createView("testview", "empdept", 
//		dbv.createSubView("sbview", 0);

		
/*
		ModuleLinker lnk = new ModuleLinker();
		lnk.linkModule(newmod);

		ModuleCompiler cmp = new ModuleCompiler();
		cmp.compileModule(newmod);
*/
		
//		System.out.println(store.dump());		

//			odra.db.links.LinkManager lman = odra.db.links.LinkManager.getInstance();


	//		DBLink link = lman.createLink("linka", newmod, "192.168.1.110", 1522, "admin.dupa", "admin");
	//		DBLink link2 = lman.createLink("linkb", newmod, "192.168.1.110", 1523, "admin.dupa", "admin");

/*			OID empagg = mod.createAggregateObject("emp", mod.getDatabaseEntry(), 0);

			for (int i = 0; i < 100; i++) {
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
//			Result res = lman.getRemoteNested(link);
			
//			Result res = lman.sendQuery(link, "deref x;");

//			System.out.println(new RawResultPrinter().print(res));


		System.out.println("Database created");
	}

	private void startup() {
		try { 
			instance = new DBInstance();
			instance.startup(); 
		}
		catch (UnknownHostException ex) {
			System.out.println("*** Database instance cannot be started");
		}
	}

	private void shutdown() {
		instance.shutdown();		
	}

/*	public void importData(String mod, String doc, String par) throws FilterException {
		System.out.println("Importujemy do " + mod);
		System.out.println("-");
		System.out.println(doc);
		System.out.println("-");
		System.out.println(par);
	}
*/
	public void testCli() throws Exception {		
		createDatabase();
		startup();

		CLI cli = new CLI();
		cli.begin();

		shutdown();
	}

	public static void main(String[] args) throws Exception {
        java.lang.System.setProperty("apple.laf.useScreenMenuBar", "true");
		java.lang.System.setProperty("apple.awt.antialiasing", "false");
		java.lang.System.setProperty("apple.awt.textantialiasing", "false");
		java.lang.System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CLI");
		
		System.out.println("Test started (debug mode: " + ConfigDebug.ASSERTS + ")");

		new MLMain().testCli();
	}
}
