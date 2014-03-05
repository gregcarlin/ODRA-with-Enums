package tests.p2p;

import odra.cli.CLI;
import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.OID;
import odra.db.links.LinkManager;
import odra.db.objects.data.DBLink;
import odra.db.objects.data.DBModule;
import odra.db.objects.meta.MBStruct;
import odra.db.objects.meta.MBTypeDef;
import odra.db.objects.meta.MetaBase;
import odra.db.objects.meta.MetabaseManager;
import odra.dbinstance.DBInstance;
import odra.filters.XML.XMLResultPrinter;
import odra.network.transport.DBConnection;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.Result;
import odra.security.UserContext;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;

public class CentralIndexTest {
	
	private ObjectManager manager;
	private DefaultStore store;

	private Database db;
	private DBInstance instance;
	
	private DBModule mod = null;
	private DBLink link1, link2;
	
	private OID index_root = null;
	
	private void createDatabase() throws Exception{
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("/tmp/test.dbf");
		fileHeap.format(1024 * 1024 * 20);
		fileHeap.open();

		allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();		
		
		manager = new ObjectManager(allocator);
		manager.initialize(100);
		
		store = new DefaultStore(manager);
		store.initialize();

		// prepsare the database
		Database.initialize(store);
		Database.open(store);
		instance = new DBInstance();

		instance.startup();
 
		mod = new DBModule(Database.getModuleByName("admin").createSubmodule("t", 0));
		
		
		link1 = LinkManager.getInstance().createLink("link1", mod, "localhost", 1521, "admin", "admin");
		
		link2 = LinkManager.getInstance().createLink("link2", mod, "host", 0, "schema", "password");
		index_root = mod.createComplexObject("CentralIndex", mod.getDatabaseEntry(), 0);
	}
	
	private MetaBase createMetabase1() throws DatabaseException{
		MetaBase mb = link1.getMBLink().getMetaBase();
		MetabaseManager metamanager = new MetabaseManager(link1.getMBLink());
		MBStruct personType = new MBStruct(metamanager.createMetaStruct(3));

		personType.createField("fName", 1, 1, "string", 0);
		personType.createField("lName", 1, 1, "string", 0);
		personType.createField("age", 1, 1, "integer", 0);

		MBTypeDef td = new MBTypeDef(metamanager.createMetaTypeDef("Person", personType.getName()));
		
		return mb;
	}
	
	private MetaBase createMetabase2() throws DatabaseException{
		MetaBase mb = link2.getMBLink().getMetaBase();
		MetabaseManager metamanager = new MetabaseManager(link2.getMBLink());
		MBStruct personType = new MBStruct(metamanager.createMetaStruct(3)); 

		personType.createField("fName", 1, 1, "string", 0);
		personType.createField("lName", 1, 1, "string", 0);
		personType.createField("age", 1, 1, "integer", 0);

		MBTypeDef td = new MBTypeDef(metamanager.createMetaTypeDef("Person", personType.getName()));
		
		return mb;
	}
	
	private void createCentralIndex(MetaBase [] metabs, OID root){
		
	}
	
	public static void main(String[] args) throws Exception{
		CentralIndexTest test = new CentralIndexTest();
		
		test.createDatabase();
		MetaBase mb1 = test.createMetabase1();
		MetaBase mb2 = test.createMetabase2();
		
		test.createCentralIndex(new MetaBase[] {mb1, mb2}, test.index_root);

		test.mod.createStringObject("str", test.index_root, "sss", 0);
		
		DBConnection conn = new DBConnection("localhost",1521);
		DBRequest req = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] {"CentralIndex;", "admin.t", "default", "off"} );
		Result res = conn.sendRequest(req).getResult();
		System.out.println(new XMLResultPrinter().print(res));
		
	}

}
