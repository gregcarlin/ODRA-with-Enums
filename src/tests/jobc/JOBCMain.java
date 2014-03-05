package tests.jobc;

import java.net.UnknownHostException;
import odra.cli.CLI;
import odra.db.Database;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;

public class JOBCMain
{
	public static void main(String[] args)
	{
		JOBCMain main = new JOBCMain();
		try
		{
			main.initialize();
			main.start();
			main.createSampleData();
			
			System.out.println("JOBC test database started...");
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	private void initialize() throws Exception
	{
		DataFileHeap fileHeap;
		RevSeqFitMemManager allocator;

		fileHeap = new DataFileHeap("/tmp/test.dbf");
		fileHeap.format(10 * 1024 * 1024);
		fileHeap.open();

		allocator = new RevSeqFitMemManager(fileHeap);
		allocator.initialize();

		ObjectManager manager = new ObjectManager(allocator);
		manager.initialize(100);
		
		DefaultStore store = new DefaultStore(manager);
		store.initialize();

		Database.initialize(store);
		Database.open(store);
	}
	
	private void start() throws UnknownHostException
	{
		new DBInstance().startup();
	}
	
	private void createSampleData() throws Exception
	{
		CLI cli = new CLI();
		cli.execBatch(new String[] {"res/sampledata/batch/createM0.cli"});
		cli.begin();
	}
}
