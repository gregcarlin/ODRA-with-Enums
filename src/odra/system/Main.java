package odra.system;

import java.util.logging.Level;

import odra.db.Database;
import odra.db.indices.updating.IndexableStore;
import odra.dbinstance.DBInstance;
import odra.store.DefaultStore;
import odra.store.io.AutoExpandableHeap;
import odra.store.io.AutoExpandableLinearHeap;
import odra.store.memorymanagement.AutoExpandableMemManager;
import odra.store.memorymanagement.ConstantSizeObjectsMemManager;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;
import odra.store.sbastore.ObjectManager;
import odra.store.sbastore.SpecialReferencesManager;
import odra.store.sbastore.ValuesManager;
import odra.system.config.ConfigServer;
import odra.ws.facade.IEndpointFacade;
import odra.ws.facade.WSManagersFactory;

/**
 * This class is the official starting point of the database server.
 * Please do not add anything here.
 *
 * @author raist
 */

public class Main {
	public void createDatabase(String fname, int size) {
		try {			
			System.out.print("Creating database file ... ");
			
			DataFileHeap fileHeap = new DataFileHeap(fname);
			fileHeap.format(size);
			fileHeap.open();		

			RevSeqFitMemManager allocator = new RevSeqFitMemManager(fileHeap);
			allocator.initialize();
			
			ObjectManager manager = new ObjectManager(allocator);
			manager.initialize(0);

			DefaultStore store = new DefaultStore(manager);
			store.initialize();

			Database.initialize(store);

			store.close();
			manager.close();

			System.out.println("Finished!");
		}
		catch (Exception ex) {
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database creation", ex);
		}
	}

	public void createOptimizedDatabase(String fname_prefix, int size_oid, int size_val, int size_spec) {
		try {
			System.out.print("Creating database file ... ");
			
			DataFileHeap fileHeap = new DataFileHeap(fname_prefix + "_objs.dbf");
			fileHeap.format(size_oid);
			fileHeap.open();			

			ConstantSizeObjectsMemManager allocator = new ConstantSizeObjectsMemManager(fileHeap, ObjectManager.MAX_OBJECT_LEN);
			allocator.initialize();
			
			DataFileHeap valuesFileHeap = new DataFileHeap(fname_prefix + "_vals.dbf");
			valuesFileHeap.format(size_val);
			valuesFileHeap.open();
			RevSeqFitMemManager valuesAllocator = new RevSeqFitMemManager(valuesFileHeap);
			valuesAllocator.initialize();
			ValuesManager valuesManager = new ValuesManager(valuesAllocator);

			DataFileHeap specFileHeap = new DataFileHeap(fname_prefix + "_spec.dbf");
			specFileHeap.format(size_spec);
			specFileHeap.open();
			ConstantSizeObjectsMemManager specAllocator = new ConstantSizeObjectsMemManager(specFileHeap, SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
			specAllocator.initialize();
			SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);
			
			ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
			manager.initialize(0);

			DefaultStore store = new DefaultStore(manager);
			store.initialize();

			Database.initialize(store);

			store.close();
			manager.close();

			System.out.println("Finished!");
		}
		catch (Exception ex) {
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database creation", ex);
		}
	}

	public void createExpandableDatabase(String fname_prefix, int size_oid, int size_val, int size_spec) {
		try {
			System.out.print("Creating database file ... ");
			
			AutoExpandableHeap fileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_objs");
			fileHeap.format(size_oid);
			fileHeap.open();			
			AutoExpandableMemManager allocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(fileHeap, ObjectManager.MAX_OBJECT_LEN);
			allocator.initialize();
			
			AutoExpandableHeap valuesFileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_vals");
			valuesFileHeap.format(size_val);
			valuesFileHeap.open();
			AutoExpandableMemManager valuesAllocator = AutoExpandableMemManager.startAutoExpandableRevSeqFitMemManager(valuesFileHeap);
			valuesAllocator.initialize();
			ValuesManager valuesManager = new ValuesManager(valuesAllocator);
			
			AutoExpandableHeap specFileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_spec");
			specFileHeap.format(size_spec);
			specFileHeap.open();
			AutoExpandableMemManager specAllocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(specFileHeap, SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
			specAllocator.initialize();
		    SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);
			
			ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
			manager.initialize(0);

			DefaultStore store = new DefaultStore(manager);
			store.initialize();

			Database.initialize(store);

			store.close();
			manager.close();

			System.out.println("Finished!");
		}
		catch (Exception ex) {
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database creation", ex);
		}
	}
	
	public void startDatabaseInstance(String fname) {
		try {
			System.out.print("Starting database instance ... ");

			DataFileHeap fileHeap = new DataFileHeap(fname);
			fileHeap.open();

			RevSeqFitMemManager allocator = new RevSeqFitMemManager(fileHeap);

			ObjectManager manager = new ObjectManager(allocator);
			manager.open();
			
			DefaultStore store = new IndexableStore(manager);
			store.open();

			Database.open(store);
		
			DBInstance instance = new DBInstance();
			instance.startup();

			System.out.println("Finished!");
			System.out.println("Listening for user connections ...");
		}
		catch (Exception ex) {
			IEndpointFacade em = WSManagersFactory.createEndpointManager();
			if (em != null) {
				em.stopServer();
			}
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database instance startup", ex);
		
		}
	}

	public void startOptimizedDatabaseInstance(String fname_prefix) {
		try {
			System.out.print("Starting database instance ... ");

			DataFileHeap fileHeap = new DataFileHeap(fname_prefix + "_objs.dbf");
			fileHeap.open();

			ConstantSizeObjectsMemManager allocator = new ConstantSizeObjectsMemManager(fileHeap, ObjectManager.MAX_OBJECT_LEN);
			
			DataFileHeap valuesFileHeap = new DataFileHeap(fname_prefix + "_vals.dbf");

			valuesFileHeap.open();

			RevSeqFitMemManager valuesAllocator = new RevSeqFitMemManager(valuesFileHeap);
			ValuesManager valuesManager = new ValuesManager(valuesAllocator);

			DataFileHeap specFileHeap = new DataFileHeap(fname_prefix + "_spec.dbf");
			specFileHeap.open();
			ConstantSizeObjectsMemManager specAllocator = new ConstantSizeObjectsMemManager(specFileHeap, SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		    SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);
			
			ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
			manager.open();

			DefaultStore store = new IndexableStore(manager);
			store.open();

			Database.open(store);
		
			
			DBInstance instance = new DBInstance();
			instance.startup();

			System.out.println("Finished!");
			System.out.println("Listening for user connections ...");
		}
		catch (Exception ex) {
			IEndpointFacade em = WSManagersFactory.createEndpointManager();
			if (em != null) {
				em.stopServer();
			}
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database instance startup", ex);
		}
	}

	public void startExpandableDatabaseInstance(String fname_prefix) {
		try {
			System.out.print("Starting database instance ... ");

			AutoExpandableHeap fileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_objs");
			fileHeap.open();

			AutoExpandableMemManager allocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(fileHeap, ObjectManager.MAX_OBJECT_LEN);
			
			AutoExpandableHeap valuesFileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_vals");
			valuesFileHeap.open();
			AutoExpandableMemManager valuesAllocator = AutoExpandableMemManager.startAutoExpandableRevSeqFitMemManager(valuesFileHeap);
			ValuesManager valuesManager = new ValuesManager(valuesAllocator);

			AutoExpandableHeap specFileHeap = AutoExpandableLinearHeap.startPersistantHeap(fname_prefix + "_spec");
			specFileHeap.open();
			AutoExpandableMemManager specAllocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(specFileHeap, SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
		    SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);
			
			ObjectManager manager = new ObjectManager(allocator, valuesManager, specManager);
			manager.open();

			DefaultStore store = new IndexableStore(manager);
			store.open();

			Database.open(store);
			
			DBInstance instance = new DBInstance();
			instance.startup();

			System.out.println("Finished!");
			System.out.println("Listening for user connections ...");
		}
		catch (Exception ex) {
			IEndpointFacade em = WSManagersFactory.createEndpointManager();
			if (em != null) {
				em.stopServer();
			}
			System.out.println("Error (" + ex.getMessage() + ")");

			ConfigServer.getLogWriter().getLogger().log(Level.SEVERE, "Exception during database instance startup", ex);
		}
	}
	
	
	private void begin(String[] args) {
		try {
			if (args.length == 3 && args[0].equals("--create")) {
				String file = args[1];
				int size = Integer.valueOf(args[2]);

				createDatabase(file, size);

				return;
			}
			else if (args.length >= 2 && args[0].equals("--start")) {
				String file = args[1];

				if (args.length > 2)
					ConfigServer.LSNR_PORT = Integer.parseInt(args[2]);

				startDatabaseInstance(file);

				return;
			} if (args.length == 5 && args[0].equals("--create_optimized")) {
				String file_prefix = args[1];
				int size_oid = Integer.valueOf(args[2]);
				int size_val = Integer.valueOf(args[3]);
				int size_spec = Integer.valueOf(args[4]);
				
				createOptimizedDatabase(file_prefix, size_oid, size_val, size_spec);

				return;
			}
			
			else if (args.length >= 2 && args[0].equals("--start_optimized")) {
				String file_prefix = args[1];

				if (args.length > 2)
					ConfigServer.LSNR_PORT = Integer.parseInt(args[2]);

				startOptimizedDatabaseInstance(file_prefix);

				return;
			}  if (args.length == 5 && args[0].equals("--create_expandable")) {
				String file_prefix = args[1];
				int size_oid = Integer.valueOf(args[2]);
				int size_val = Integer.valueOf(args[3]);
				int size_spec = Integer.valueOf(args[4]);
				
				createExpandableDatabase(file_prefix, size_oid, size_val, size_spec);

				return;
			}
			
			else if (args.length >= 2 && args[0].equals("--start_expandable")) {
				String file_prefix = args[1];

				if (args.length > 2)
					ConfigServer.LSNR_PORT = Integer.parseInt(args[2]);

				startExpandableDatabaseInstance(file_prefix);

				return;
			}
		}
		catch (NumberFormatException ex) {
			System.out.println("*** Invalid number format");
		}

		System.out.println("usage:");
		System.out.println("  --create <database file name> <size>");
		System.out.println("  --create_optimized <database file name prefix> <size for oids> <size for values> <size for specrefs>");
		System.out.println("  --create_expandable <database file name prefix> <size for oids> <size for values> <size for specrefs>");
		System.out.println("  --start <database file name> { port }");
		System.out.println("  --start_optimized <database file name prefix> { port }");
		System.out.println("  --start_expandable <database file name prefix> { port }");
		
	}
	


	public static void main(String[] args) {
		new Main().begin(args);
	}
}
