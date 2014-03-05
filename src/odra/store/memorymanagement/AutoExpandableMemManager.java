package odra.store.memorymanagement;

import java.util.Vector;

import odra.db.DatabaseException;
import odra.store.io.AutoExpandableHeap;
import odra.store.io.IHeap;

/**
 * This class contains a autoexpandable memory allocator managing 
 * and generating individual memory managers.
 * 
 * @author tkowals
 */

public class AutoExpandableMemManager extends AbstractMemoryManager {

	Vector<IMemoryManager> memmgrs = new Vector<IMemoryManager>(); 

	AutoExpandableHeap autoexpHeap;
	
	int maxobjsize;
	
	int memmanager_kind; // REVSEQFIT_MM or CONSTANTSIZEOBJS_MM;
	
	/**
	 * Initializes the memory allocator using an object representing
	 * the space the allocator is supposed to manage.
	 * Uses multiple RevSeqFitMemManagers
	 * @throws DatabaseException 
	 */
	private AutoExpandableMemManager(AutoExpandableHeap autoexpHeap) throws DatabaseException {
		super(autoexpHeap);
		this.autoexpHeap = autoexpHeap;
		this.memmanager_kind = REVSEQFIT_MM;
		for(IHeap heap : autoexpHeap.getHeapsCollection())
			memmgrs.add(new RevSeqFitMemManager(heap, REVSEQMINFREE_FACTOR));
	}
	
	public static AutoExpandableMemManager startAutoExpandableRevSeqFitMemManager(AutoExpandableHeap autoexpHeap) throws DatabaseException {
		return new AutoExpandableMemManager(autoexpHeap);
	}
	
	/**
	 * Initializes the memory allocator using an object representing
	 * the space the allocator is supposed to manage.
	 * Uses multiple ConstansSizeObjectsMemManagers
	 * @throws DatabaseException 
	 */
	private AutoExpandableMemManager(AutoExpandableHeap autoexpHeap, int maxobjsize) throws DatabaseException {
		super(autoexpHeap);
		this.autoexpHeap = autoexpHeap;
		this.memmanager_kind = CONSTANTSIZEOBJS_MM;
		this.maxobjsize = maxobjsize;
		for(IHeap heap : autoexpHeap.getHeapsCollection())
			memmgrs.add(new ConstantSizeObjectsMemManager(heap, maxobjsize));
	}
	
	public static AutoExpandableMemManager startAutoExpandableConstantSizeObjectsMemManager(AutoExpandableHeap autoexpHeap, int maxobjsize) throws DatabaseException {
		return new AutoExpandableMemManager(autoexpHeap, maxobjsize);
	}
	
	public void initialize() {
		for(IMemoryManager memmgr : memmgrs)
			memmgr.initialize();
	}
	
	public int getEntryOffset() {
		return memmgrs.firstElement().getEntryOffset();
	}

	
	public void setEntryOffset(int value) {
		memmgrs.firstElement().setEntryOffset(value);

	}

	public boolean staticRealloc(int offset, int nbytes)
	throws DatabaseException {
		int num = autoexpHeap.findHeapNum(offset);
		return memmgrs.elementAt(num).staticRealloc(autoexpHeap.relativeOffset(num, offset), nbytes);
	}
	
	public int malloc(int nbytes) throws DatabaseException {
		int offset;
		for(int i = memmgrs.size() - 1; i >= 0 ; i--) {
			IMemoryManager memmgr = memmgrs.elementAt(i);
			try {
				offset = memmgr.malloc(nbytes);
				return autoexpHeap.getStartOffset(i) + offset;
			} catch (DatabaseException e) {} 
		}
		
		if (memmanager_kind == CONSTANTSIZEOBJS_MM) 
			memmgrs.add(new ConstantSizeObjectsMemManager(autoexpHeap.expand(), maxobjsize));
		else if (memmanager_kind == REVSEQFIT_MM)
			memmgrs.add(new RevSeqFitMemManager(autoexpHeap.expand(), REVSEQMINFREE_FACTOR));
	 			
		memmgrs.lastElement().initialize();
		
		int i = memmgrs.size() - 1;
		
		return autoexpHeap.getStartOffset(i) + memmgrs.lastElement().malloc(nbytes);
	}

	public int malloc(int offset, int nbytes) throws DatabaseException {
	
		int num = autoexpHeap.findHeapNum(offset);
		return autoexpHeap.getStartOffset(num) + memmgrs.elementAt(num).malloc(autoexpHeap.relativeOffset(num, offset), nbytes);
		
	}
	
	public int realloc(int offset, int nbytes) throws DatabaseException {
		
		if (staticRealloc(offset, nbytes))
			return offset;
				
		int noffset = malloc(nbytes * 2);  // * 2 is a memory optimization tweak for objects dynamically changing size					

		setData(noffset, getData(offset));	
		free(offset);
		return noffset;
		
	}

	public int falloc(int offset, int nbytes) throws DatabaseException {

		if (staticRealloc(offset, nbytes)) 
			return offset;
		
		free(offset);
			
		return malloc(nbytes * 2); // * 2 is a memory optimization tweak for objects dynamically changing size
		
	}

	public int free(int offset) {
		int num = autoexpHeap.findHeapNum(offset);
		return autoexpHeap.getStartOffset(num) + memmgrs.elementAt(num).free(autoexpHeap.relativeOffset(num, offset));
	}

	public byte[] getData(int offset) {
		
		int num = autoexpHeap.findHeapNum(offset);
		return ((RevSeqFitMemManager) (memmgrs.elementAt(num))).getData(autoexpHeap.relativeOffset(num, offset));
		
	}

	public void setData(int offset, byte[] buf) {
		
		int num = autoexpHeap.findHeapNum(offset);
		((RevSeqFitMemManager) (memmgrs.elementAt(num))).setData(autoexpHeap.relativeOffset(num, offset), buf);
		
	}
	
	/**
	 * Returns a total memory size.
	 * 
	 * @return total memory
	 */
	public int getTotalMemory()
	{
		int memory = 0;
		
		for(IMemoryManager memmgr : memmgrs)
			memory += memmgr.getTotalMemory();
		
		return memory;
	}
	
	/**
	 * Returns a free memory size.
	 * 
	 * @return free memory
	 */
	public int getFreeMemory()
	{
		int free = 0;
		
		for(IMemoryManager memmgr : memmgrs)
			free += memmgr.getFreeMemory();
		
		return free;
	}
	
	/**
	 * Returns an used memory size.
	 * 
	 * @return used memory
	 */
	public int getUsedMemory()
	{
		int usedmemory = 0;
		
		for(IMemoryManager memmgr : memmgrs)
			usedmemory += memmgr.getUsedMemory();
		
		return usedmemory;
	}
	
	public String dump(boolean verbose) {
		StringBuffer buf = new StringBuffer();
		
		for(IMemoryManager memmgr : memmgrs)
			buf.append(memmgr.dump(verbose));
		
		// TODO Add summary free memory information!
		return buf.toString();
	}

	public Vector<Integer> getObjectsInSequence() throws DatabaseException {
		Vector<Integer> objs = new Vector<Integer>();
		Vector<Integer> auxobjs;
		int startoffset, auxcount;
		
		for(int i = 0; i < memmgrs.size() ; i++) {
			IMemoryManager memmgr = memmgrs.elementAt(i);
			auxobjs = memmgr.getObjectsInSequence();
			startoffset = autoexpHeap.getStartOffset(i);
			auxcount = auxobjs.size();
			for(int j = 0; j < auxcount; j++)
				auxobjs.set(j, auxobjs.elementAt(j) + startoffset);
				
			objs.addAll(auxobjs);	 
		} 
			
		return objs;
	} 
	
	public static int REVSEQFIT_MM = 1;
	public static int CONSTANTSIZEOBJS_MM = 2;
	
	/* set between(0 - 99):
	 * less - slower allocation, less space wasted
	 * more - faster allocation, more space wasted
	 */ 
	private static int REVSEQMINFREE_FACTOR = 15;
	
}
