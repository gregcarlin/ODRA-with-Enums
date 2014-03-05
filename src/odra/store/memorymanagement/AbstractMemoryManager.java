package odra.store.memorymanagement;

import odra.store.io.IHeap;

public abstract class AbstractMemoryManager implements IMemoryManager {

	protected final IHeap heap;

	public AbstractMemoryManager(IHeap heap) {
		this.heap = heap;
	}

	/**
	    * Returns the heap.
	    * 
	    * @return heap
	    */
	public IHeap getHeap() {
	      return heap;
	   }

}