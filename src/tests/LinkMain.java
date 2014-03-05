package tests;

import java.net.UnknownHostException;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MBVariable;
import odra.db.objects.meta.MetaBase;
import odra.db.objects.meta.MetabaseManager;
import odra.db.schema.OdraProcedureSchema;
import odra.db.schema.OdraTypeSchema;
import odra.db.schema.OdraVariableSchema;
import odra.db.schema.OdraViewSchema;
import odra.db.schema.ProcArgument;
import odra.db.schema.OdraProcedureSchema.ProcedureAST;
import odra.dbinstance.DBInstance;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.terminals.Name;
import odra.sbql.builder.BuilderUtils;
import odra.sbql.builder.ModuleOrganizer;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigDebug;

public class LinkMain // implements DataImporter
{
	private ObjectManager manager;

	private DefaultStore store;

	private Database db;

	private DBInstance instance;

	private void createDatabase() throws Exception
	{

		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("test.dbf");
		fileHeap.format(1024 * 1024 * 10);
		fileHeap.open();

		allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();

		manager = new ObjectManager(allocator);
		manager.initialize(1000);

		store = new DefaultStore(manager);
		store.initialize();

		// prepare the database
		Database.initialize(store);
		Database.open(store);

		System.out.println("Database created");

		DBModule mod = Database.getModuleByName("admin");
		MetabaseManager metamanager = new MetabaseManager(mod);

		metamanager.createMetaVariable(new OdraVariableSchema("global_X","integer", 1, 1,  0));
		mod.createIntegerObject("global_X", mod.getDatabaseEntry(), 12);

		metamanager.createMetaVariable(new OdraVariableSchema("z", "integer",1, 1,  0));
		mod.createIntegerObject("z", mod.getDatabaseEntry(), 13);

		DBModule testmod = new DBModule(mod.createSubmodule("test", 0));
	}

	private void createSampleMetadata() throws Exception
	{

		DBModule testmod = Database.getModuleByName("admin.test");
		MetabaseManager metamanager = new MetabaseManager(testmod);
		metamanager.createMetaVariable(new OdraVariableSchema("x",  "integer", 1, 1, 0));
		testmod.createIntegerObject("x", testmod.getDatabaseEntry(), 12);

		metamanager.createMetaVariable(new OdraVariableSchema("y", "string",1, 1, 0));
		testmod.createStringObject("y", testmod.getDatabaseEntry(), "ODRA", 0);

		MBStruct personType = new MBStruct(metamanager.createMetaStruct(3));

		personType.createField("fName", 1, 1, "string", 0);
		personType.createField("lName", 1, 1, "string", 0);
		personType.createField("age", 1, 1, "integer", 0);

		MBTypeDef td = new MBTypeDef(metamanager.createMetaTypeDef("PersonType", personType.getName()));

		MBVariable pmbv = new MBVariable(metamanager.createMetaVariable(new OdraVariableSchema("Person", td.getName(),0, Integer.MAX_VALUE,  0)));

		MBTypeDef td2 = new MBTypeDef(metamanager.createMetaTypeDef("EmpType", personType.getName()));

		MBVariable pmbv2 = new MBVariable(metamanager.createMetaVariable(new OdraVariableSchema("Emp", td2.getName(),0, Integer.MAX_VALUE,  0)));
		
		System.out.println("module serial " + testmod.getSerial());

	}

	private void testCreateView() throws Exception
	{
		DBModule mod = Database.getModuleByName("admin.test");
		// Session.create();
		// Session.initialize("admin", "admin");
		ModuleOrganizer org = new ModuleOrganizer(mod, true);

		
		OdraProcedureSchema vo = new OdraProcedureSchema("ViewCar", new ProcArgument[0],
			new ProcedureAST(BuilderUtils.serializeAST(new ReturnWithValueStatement(new AsExpression(new NameExpression(new Name("Car")),
				new Name("c"))))), new OdraTypeSchema("Car", 0, Integer.MAX_VALUE, 0));
		OdraViewSchema view = new OdraViewSchema("ViewCarDef", vo);
		OdraProcedureSchema retr = new OdraProcedureSchema(OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString(), new ProcArgument[0], new ProcedureAST(BuilderUtils.serializeAST(new ReturnWithValueStatement(new DerefExpression(new DotExpression(
			new NameExpression(new Name("c")), new NameExpression(new Name("model"))))))), 
				 new OdraTypeSchema("string", 1, 1, 0));
		view.addGenericProcedure(retr);
		org.createView(view);
		mod.setModuleCompiled(false);
		mod.setModuleLinked(false);

		mod.setModuleCompiled(false);
		mod.setModuleLinked(false);
		// Session.close();
	}

	private void testsendQuery() throws Exception
	{
		DBModule mod = Database.getModuleByName("admin");

		LinkManager lman = LinkManager.getInstance();

		DBLink linka = lman.createLink("linka", mod, "localhost", 1521, "admin.test", "admin");
		DBLink linkb = lman.createLink("linkb", mod, "localhost", 1521, "admin.test", "admin");
		

		System.out.println("Link created");
		
//		ReferenceResult refRes = (ReferenceResult) lman.sendQuery(linka, "x;",0,null, new UserContext("admin", "admin.test"));
//		System.out.println("remote query executed");
		
//		DBModule testmod = Database.getModuleByName("admin.test");
//		testmod.createMetaVariable("xe", 1, 1, "integer", 0);
//		testmod.createIntegerObject("xe", testmod.getDatabaseEntry(), 12);
//
//		try
//		{
//			refRes = (ReferenceResult) lman.sendQuery(linka, "x;", new UserContext("admin", "admin.test"));
//			System.out.println("remote query executed, after changing metabase");
//		}
//		catch (RDStaleMetaBaseException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		
//		 IntegerResult intRes = (IntegerResult) lman.sendQuery(linka, "deref x;", new UserContext("admin", "admin.test"));
//		//
//		 StructResult strucRes = (StructResult) lman.sendQuery(linka, "x,y;", new UserContext("admin", "admin"));
//		//
		// System.out.println("Result of refRes \t" + new RawResultPrinter().print(refRes));
		// System.out.println("der\t " + refRes.value.derefInt());
		// System.out.println("Result of intRes \t" + new RawResultPrinter().print(intRes));
		//
		// System.out.println("Result of StructResult \t" + new RawResultPrinter().print(strucRes));

		// lman.getMetadata(link, new UserContext("admin", "admin"));
	}

	private void startup()
	{
		try
		{
			instance = new DBInstance();
			instance.startup();
		}
		catch (UnknownHostException ex)
		{
			System.out.println("*** Database instance cannot be started");
		}
	}
	
	private void testRefreshLinks() throws Exception
	{
	
		
//		odra.cli.CLI cli = new odra.cli.CLI();
//		try
//		{
//			cli.execBatch(new String[] { "res/sampledata/batch/link.cli" });
//			//cli.begin();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			throw e;			
//		}
		
		
		Thread.sleep(9000);
		System.out.println("start");
		
		DBModule mod = Database.getModuleByName("admin");

		System.out.println("links " );
		LinkManager lman = LinkManager.getInstance();

//		DBLink link1 = lman.createLink("link1", mod, "localhost", 1521, "admin.test", "admin");
//		DBLink link2 = lman.createLink("link2", mod, "localhost", 1521, "admin.test", "admin");
//		DBLink link3 = lman.createLink("link3", mod, "localhost", 1521, "admin.test", "admin");
//		DBLink link4 = lman.createLink("link4", mod, "localhost", 1521, "admin.test", "admin");
		
		for ( int i = 0 ; i<300 ; i++)
		{
			
			
			DBLink link1 = lman.createLink("link1", mod, "localhost", 1521, "admin.test", "admin");
			DBLink link2 = lman.createLink("link2", mod, "localhost", 1521, "admin.test", "admin");
			DBLink link3 = lman.createLink("link3", mod, "localhost", 1521, "admin.test", "admin");
			DBLink link4 = lman.createLink("link4", mod, "localhost", 1521, "admin.test", "admin");
			
//			lman.refreshLinkMetadata(link1);
//			lman.refreshLinkMetadata(link2);
//			lman.refreshLinkMetadata(link3);
//			lman.refreshLinkMetadata(link4);
			
			lman.removeLink("link1", mod.getName());
			lman.removeLink("link2", mod.getName());
			lman.removeLink("link3", mod.getName());
			lman.removeLink("link4", mod.getName());
			
			
			System.out.println("done " +i);
		}

		
	}

	private void shutdown()
	{
		instance.shutdown();
	}

	public void testCli() throws Exception
	{

		createDatabase();
		startup();

		createSampleMetadata();
		// testCreateView();
//		testsendQuery();
		
		testRefreshLinks();

		CLI cli = new CLI();
		cli.begin();

		shutdown();
	}

	public static void main(String[] args) throws Exception
	{
//		java.lang.System.setProperty("apple.laf.useScreenMenuBar", "true");
//		java.lang.System.setProperty("apple.awt.antialiasing", "false");
//		java.lang.System.setProperty("apple.awt.textantialiasing", "false");
//		java.lang.System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CLI");

		System.out.println("Test started (debug mode: " + ConfigDebug.ASSERTS + ")");

		new LinkMain().testCli();
	}
}
