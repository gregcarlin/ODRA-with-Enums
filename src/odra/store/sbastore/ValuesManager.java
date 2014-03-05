package odra.store.sbastore;

import odra.db.DatabaseException;
import odra.store.io.IHeap;
import odra.store.memorymanagement.AbstractMemoryManager;
import odra.store.memorymanagement.IMemoryManager;
import odra.system.Sizes;

/**
 * 
 * Values-space which stores values of binary and string objects 
 * and lists of backward and children references  
 * A sample, persistent, data store, built in accordance with the M0 SBA data model
 * It introduces seperation on object-space and values-space.
 * Object-space stores ODRA objects accessible by OIDs (more or less of constant size)
 * 
 * @author tkowals
 * @version 1.0
 */
public class ValuesManager {

	private final IMemoryManager valuesAllocator;
	private final IHeap valuesHeap;
	
	private IObjectManager objectManager;
	
	public ValuesManager(AbstractMemoryManager valuesAllocator) {
		this.valuesAllocator = valuesAllocator;
		this.valuesHeap = valuesAllocator.getHeap();
	}
	
	public void setObjectManager(IObjectManager objectManager) {
		this.objectManager = objectManager;
	}
	
	void open() throws DatabaseException {
	
	}
	
	void close() {
		valuesHeap.close();
	}
	
	IMemoryManager getValuesMemoryManager() {
		return valuesAllocator;
	}
	
	void deleteComplexObjectValue(int offset) throws DatabaseException {
		int refval = objectManager.getIntegerObjectValue(offset);
		
		if (refval != 0)
			valuesAllocator.free(refval);	
	}
	
	void deleteBinaryObjectValue(int offset) throws DatabaseException {
		int refval = objectManager.getIntegerObjectValue(offset);
		
		if (refval != 0)
			valuesAllocator.free(refval);	
	}

	void deleteStringObjectValue(int offset) throws DatabaseException {
		int refval = objectManager.getIntegerObjectValue(offset);
		
		if (refval != 0)
			valuesAllocator.free(refval);
	}
	
	/* *************************************************************************************
	 * this part is for blocks of integers (used to model backward references and complex object values)
	 * */
	
	// returns the number of integers stored in a particular block.
	// the length is stored as the first integer (first 4 bytes).
	final int countInts(int boffset) throws DatabaseException {
		return boffset == 0 ? 0 : valuesHeap.readInteger(boffset);
	}

	// returns value stored in the block
	final int getIntFromBlockOfInts(int boffset, int index) throws DatabaseException {
		return valuesHeap.readInteger(boffset + Sizes.INTVAL_LEN + Sizes.INTVAL_LEN * index);
	}	
	
	// returns all values stored in the block
	final int[] getIntsFromBlock(int boffset) throws DatabaseException {
		if (boffset == 0) return new int[0];
		
		int nints = valuesHeap.readInteger(boffset);
		
		int[] ints = new int[nints];		
		for (int i = 0; i < nints; i++)
			ints[i] = valuesHeap.readInteger(boffset + Sizes.INTVAL_LEN + Sizes.INTVAL_LEN * i);
		
		return ints;
	}
	
	// returns first value stored in the block or 0
	final int getFirstIntFromBlock(int boffset) throws DatabaseException {
		if (boffset == 0) return 0;
		
		if (valuesHeap.readInteger(boffset) == 0)
			return 0;
		
		return valuesHeap.readInteger(boffset + Sizes.INTVAL_LEN);
	}
	
	//	 allocates a block of memory so that it could store int values
	// (but the first integer indicating the number of values is set to zero).
	// used to speed up memory allocations/deallocations.
	final int preallocateBlockOfInts(int ints) throws DatabaseException {
		if (ints > 0) {
			int chaddr = valuesAllocator.malloc(Sizes.INTVAL_LEN * (ints + 1));
			valuesHeap.writeInteger(chaddr, 0);
			
			return chaddr;
		}
		
		return 0;
	}
	
	// removes first occurance of a particular value from a block of integers
	final int removeIntFromBlockOfInts(int boffset, int val) throws DatabaseException {
		if (boffset == 0) return 0;
		
		int nints = valuesHeap.readInteger(boffset);
		int oneint;

		for (int i = 0; i < nints; i++) {
			oneint = valuesHeap.readInteger(boffset + Sizes.INTVAL_LEN + i * Sizes.INTVAL_LEN);

			if (oneint == val) {
				if (nints == 1) {
					valuesAllocator.free(boffset);
					return 0;
				} else {
				
					if (i < nints - 1) {
						byte[] buf = new byte[(nints - (i + 1)) * Sizes.INTVAL_LEN];
						valuesHeap.read(boffset + Sizes.INTVAL_LEN + (i + 1) * Sizes.INTVAL_LEN, buf);
						valuesHeap.write(boffset + Sizes.INTVAL_LEN + i * Sizes.INTVAL_LEN, buf);
					}
				
					int newboffset = valuesAllocator.realloc(boffset, nints * Sizes.INTVAL_LEN);
					valuesHeap.writeInteger(newboffset, nints - 1);
					
					return newboffset;
				}
			}
		}
		
		System.err.println("Couldn't find " + val + " in a block of ints");
		
		return 0;
	}

	// adds a new value to a block of integers
	final int appendIntToBlockOfInts(int boffset, int val) throws DatabaseException {

		int ints = 0;
		int nboffset;

		if (boffset != 0) {
			ints = valuesHeap.readInteger(boffset);
			
			nboffset = valuesAllocator.realloc(boffset, Sizes.INTVAL_LEN * (ints + 2));
		}
		else
			nboffset = valuesAllocator.malloc(Sizes.INTVAL_LEN * (ints + 2));
		
		valuesHeap.writeInteger(nboffset, ints + 1);
		valuesHeap.writeInteger(nboffset + Sizes.INTVAL_LEN + Sizes.INTVAL_LEN * ints, val);
		
		return nboffset;
	}

	// adds a new value in the beginning of a block of integers
	final int insertIntToBlockOfInts(int boffset, int val) throws DatabaseException {

		int ints = 0;
		int nboffset;

		if (boffset != 0) {
			ints = valuesHeap.readInteger(boffset);
			
			nboffset = valuesAllocator.realloc(boffset, Sizes.INTVAL_LEN * (ints + 2));
		}
		else
			nboffset = valuesAllocator.malloc(Sizes.INTVAL_LEN * (ints + 2));

		
		if (ints > 0) {
			byte[] buf = new byte[ints * Sizes.INTVAL_LEN];
			valuesHeap.read(boffset + Sizes.INTVAL_LEN, buf);
			valuesHeap.write(nboffset + Sizes.INTVAL_LEN + Sizes.INTVAL_LEN, buf);
		}
		
		valuesHeap.writeInteger(nboffset, ints + 1);
		valuesHeap.writeInteger(nboffset + Sizes.INTVAL_LEN, val);		

		return nboffset;
	}
	
	//	 allocates a block of memory so that it could store binary values
	// (but the first integer indicating the number of values is set to zero).
	// used to speed up memory allocations/deallocations.
	final int preallocateBlockOfBytes(int bytes) throws DatabaseException {
		if (bytes > 0) {
			int chaddr = valuesAllocator.malloc(Sizes.INTVAL_LEN + bytes);
			valuesHeap.writeInteger(chaddr, 0);
			
			return chaddr;
		}
		
		return 0;
	}
	
	/* *************************************************************************************
	 * Binary object value management
	 * */
	
	final void setBinaryObjectValue(int offset, byte[] val) throws DatabaseException {
		int valaddr = objectManager.getIntegerObjectValue(offset);
		
		if (valaddr == 0)
			valaddr = valuesAllocator.malloc(val.length + Sizes.INTVAL_LEN);
		else 
			valaddr = valuesAllocator.falloc(valaddr, val.length + Sizes.INTVAL_LEN);
		
		valuesHeap.writeBytesWithLength(valaddr, val);
		
		objectManager.setIntegerObjectValue(offset, valaddr);
	}
	
	final byte[] getBinaryObjectValue(int offset) throws DatabaseException {
		int valaddr = objectManager.getIntegerObjectValue(offset);

		if (valaddr == 0)
			return new byte[0];
		
		return valuesHeap.readBytesWithLength(valaddr);
	}

	/* *************************************************************************************
	 * this part is used for debugging purposes
	 * */
	
	final String dumpBlockOfInts(int boffset) throws DatabaseException {
		if (boffset == 0) 
			return "values: " + 0 + ", no values";
		
		int nints = valuesHeap.readInteger(boffset);
		
		String str = "values: " + nints + ", values: ";
		
		for (int i = 0; i < nints; i++)
			str += valuesHeap.readInteger(boffset + Sizes.INTVAL_LEN + Sizes.INTVAL_LEN * i) + " ";
		
		return str;
	}
	
	final String dumpMemory(boolean verbose) throws DatabaseException {
		return valuesAllocator.dump(verbose);
	}
	
	final IMemoryManager getMemoryManager() {
		return valuesAllocator;
	}

	final int getFreeMemory() {
		return valuesAllocator.getFreeMemory();
	}

	final int getTotalMemory() {
		return valuesAllocator.getTotalMemory();
	}

	final int getUsedMemory() {
		return valuesAllocator.getUsedMemory();
	}	
}