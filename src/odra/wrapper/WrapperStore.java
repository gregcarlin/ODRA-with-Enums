package odra.wrapper;

import odra.db.DatabaseException;
import odra.db.OID;
import odra.store.TransientStore;
import odra.store.io.AutoExpandableHeap;
import odra.store.io.AutoExpandableLinearHeap;
import odra.store.io.AutoExpandablePowerHeap;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.store.memorymanagement.AutoExpandableMemManager;
import odra.store.memorymanagement.IMemoryManager;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.sbastore.ObjectManager;
import odra.store.sbastore.SpecialReferencesManager;
import odra.store.sbastore.ValuesManager;
import odra.store.transience.DataMemoryBlockHeap;

/**
 * Wrapper memory storage manager.  
 * @author jacenty
 * @version   2006-03-10
 * @since   2006-03-08
 */
public class WrapperStore
{

	/** store */
	private final TransientStore store;
	/** store root */
	private final OID root;
	
	/**
	 * The constructor.
	 * 
	 * @param size size
	 * @throws DatabaseException
	 */
	WrapperStore(int size) throws DatabaseException
	{
		DataMemoryBlockHeap heap = new DataMemoryBlockHeap(size);
		AbstractMemoryManager memoryManager = new RevSeqFitMemManager(heap);
		memoryManager.initialize();
		ObjectManager objectManager = new ObjectManager(memoryManager);
		store = new TransientStore(objectManager);
		
		root = store.createAggregateObject(store.addName("$root"), store.getEntry(), 0);
	}

	/**
	 * The constructor for optimized, expandable transient heap
	 * 
	 * @param size size
	 * @throws DatabaseException
	 */
	WrapperStore(int size_obj, int size_val, int size_spec) throws DatabaseException
	{
		ObjectManager manager = null;
		
		AutoExpandableHeap dmbHeap = AutoExpandableLinearHeap.initializeTransientHeap(size_obj);
		
		AutoExpandableMemManager allocator;

		allocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(dmbHeap, ObjectManager.MAX_OBJECT_LEN);
		allocator.initialize();
		
		AutoExpandableHeap valuesdmbHeap = AutoExpandableLinearHeap.initializeTransientHeap(size_val);
		AutoExpandableMemManager valuesAllocator = AutoExpandableMemManager.startAutoExpandableRevSeqFitMemManager(valuesdmbHeap);
		valuesAllocator.initialize();
		ValuesManager valuesManager = new ValuesManager(valuesAllocator);
	      
		AutoExpandableHeap specdmbHeap = AutoExpandablePowerHeap.initializeTransientHeap(size_spec);
	    AutoExpandableMemManager specAllocator = AutoExpandableMemManager.startAutoExpandableConstantSizeObjectsMemManager(specdmbHeap, SpecialReferencesManager.MAX_SPECIALOBJECT_LEN);
	    specAllocator.initialize();
	    SpecialReferencesManager specManager = new SpecialReferencesManager(specAllocator);
	      
	    manager = new ObjectManager(allocator, valuesManager, specManager);
				
		store = new TransientStore(manager);
		
		root = store.createComplexObject(store.addName("$root"), store.getEntry(), 0);
	}	
	
	/**
	 * Creates an OID for import.
	 * 
	 * @return OID parent for import
	 * @throws DatabaseException
	 */
	OID allocate() throws DatabaseException
	{
		return store.createComplexObject(store.addName("$result"), root, 0);
	}
	
	/**
	 * Dumps the store memory.
	 * 
	 * @return dump string
	 * @throws DatabaseException
	 */
	public String dumpMemory(boolean verbose) throws DatabaseException
	{
		return store.dumpMemory(verbose);
	}
	
	/**
	 * Dumps the store.
	 * 
	 * @return dump string
	 * @throws DatabaseException
	 */
	public String dump() throws DatabaseException
	{
		return store.dump();
	}
	
	/**
	 * Returns a transient store.
	 * 
	 * @return {@link TransientStore}
	 */
	public TransientStore getTransientStore()
	{
		return store;
	}
}
