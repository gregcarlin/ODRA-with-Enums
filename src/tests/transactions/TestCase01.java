package tests.transactions;

import odra.db.Database;
import odra.db.indices.updating.IndexableStore;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.io.IHeap;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.IObjectManager;
import odra.store.sbastore.ObjectManager;
import odra.system.config.ConfigClient;
import odra.system.config.ConfigServer;
import odra.system.log.UniversalLogger;
import odra.transactions.TransactionsAssemblyInfo;
import odra.transactions.store.TransactionCapableHeap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class TestCase01 {

	private final static UniversalLogger logger = UniversalLogger.getInstance(TransactionsAssemblyInfo.class,
				TestCase01.class);

	private TransactionsCLI client;

	private DBInstance dbInstance;

	public TestCase01() {
	}

	private void createDatabase(String fileDatabase, int sizeDatabase) throws Exception {
		logger.info("Creating database file ... ");

		DataFileHeap fileHeap = new DataFileHeap(TransactionsTestConfiguration.DATABASE_FILE);
		fileHeap.format(TransactionsTestConfiguration.DATABASE_SIZE);
		fileHeap.open();

		IHeap heap = fileHeap;
		RevSeqFitMemManager allocator = new RevSeqFitMemManager(heap);
		allocator.initialize();

		ObjectManager manager = new ObjectManager(allocator);
		manager.initialize(0);

		DefaultStore store = new DefaultStore(manager);
		store.initialize();

		Database.initialize(store);

		store.close();
		manager.close();

		logger.info("Finished!");
	}

	private void startDatabase(String fileDatabase) throws Exception {
		logger.info("Starting database instance ... ");

		DataFileHeap fileHeap = new DataFileHeap(TransactionsTestConfiguration.DATABASE_FILE);
		fileHeap.open();

		IHeap heap = fileHeap;
		if (ConfigServer.TRANSACTIONS) {
			// heap = TransactionCapableHeap.getInstance(fileHeap);
			heap = fileHeap;
		}

		RevSeqFitMemManager allocator = new RevSeqFitMemManager(heap);

		IObjectManager manager = new ObjectManager(allocator);
		manager.open();

		DefaultStore store = new IndexableStore(manager);
		store.open();

		Database.open(store);

		this.dbInstance = new DBInstance();
		this.dbInstance.startup();

		logger.info("Finished!");
		logger.info("Listening for user connections ...");
	}

	private void startClient() throws Exception {
		this.client = new TransactionsCLI();
	}

	private void runTest01() throws Exception {
		this.client.execBatch(TransactionsTestConfiguration.BATCH_FILE01);
		this.client.execBatch(TransactionsTestConfiguration.BATCH_FILE02);
	}

	private void shutdownDatabase() throws Exception {
		this.dbInstance.shutdown();
	}

	@Before
	public void prepareEnvironment() {
		try {
			ConfigServer.TRANSACTIONS = true;
			this.createDatabase(TransactionsTestConfiguration.DATABASE_FILE, TransactionsTestConfiguration.DATABASE_SIZE);
			this.startDatabase(TransactionsTestConfiguration.DATABASE_FILE);
			ConfigClient.CONNECT_TIMEOUT = 30000;
			this.startClient();
		} catch (Exception ex) {
			logger.error("exception occurred", ex);
			Assert.assertTrue(false);
		}
	}

	@Test
	public void runTest() {
		try {
			this.runTest01();
		} catch (Exception ex) {
			logger.error("exception occurred", ex);
			Assert.assertTrue(false);
		}
	}

	@After
	public void destroyEnvironment() {
		try {
			this.shutdownDatabase();
		} catch (Exception ex) {
			logger.error("exception occurred", ex);
			Assert.assertTrue(false);
		}
	}
}