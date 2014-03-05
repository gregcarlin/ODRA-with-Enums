package tests.review.demo;

import java.net.UnknownHostException;
import odra.cli.CLI;
import odra.db.Database;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import tests.jobc.JOBCMain;

/**
 * Integration demo.
 * 
 * @author jacenty
 * @version 2008-01-29
 * @since 2008-01-29
 */
public class DemoMain
{
	public static void main(String[] args)
	{
		DemoMain main = new DemoMain();
		try
		{
			main.initialize();
			main.start();
			main.init();
			
			new Demo().setVisible(true);
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
	
	private void init() throws Exception
	{
		CLI cli = new CLI();
		cli.execBatch(new String[] {"res/review/cli/init.cli"});
	}
}
