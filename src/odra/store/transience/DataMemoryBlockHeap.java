package odra.store.transience;

import java.nio.ByteBuffer;

import odra.db.DatabaseException;
import odra.store.io.DataHeap;

/**
 * This class is used to construct transient data store.
 * Data stored in such a store is not durable.
 * 
 * @author raist
 */

public class DataMemoryBlockHeap extends DataHeap {

	/**
	 * Initializes a new data block
	 */
	public DataMemoryBlockHeap(int size) {
		buffer = ByteBuffer.wrap(new byte[size]);
		this.size = size; 
	}
	
	public void open() throws DatabaseException {
	}
	
	public void close() {
	    buffer = null;
	}
	
	/**
	 * @return buffer representing the content of the file
	 */
	public ByteBuffer getBuffer() throws DatabaseException {
		return buffer;
	}
	
	/**
	 * @return first, user accesible byte of the store
	 */
	public int getStartOffset() {
		return 0;
	}

	/**
	 * @return size of the heap
	 */
	public int getSize() {
		return size;
	}
	
}
