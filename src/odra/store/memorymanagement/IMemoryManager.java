package odra.store.memorymanagement;

import java.util.Vector;

import odra.db.DatabaseException;

/**
 * This interface declares memory management operations performed on the heap
 * 
 * @author raist
 */

public interface IMemoryManager {
	public void initialize();
	public String dump(boolean verbose);
	public int free(int offset);
	public int malloc(int nbytes) throws DatabaseException;
	public int malloc(int offset, int nbytes) throws DatabaseException;
	public int falloc(int offset, int nbytes) throws DatabaseException;
	public int realloc(int offset, int nbytes) throws DatabaseException;
	public boolean staticRealloc(int offset, int nbytes) throws DatabaseException;
	public byte[] getData(int offset) throws DatabaseException;
	public void setData(int offset, byte[] buf) throws DatabaseException;
	public int getEntryOffset();
	public void setEntryOffset(int vlaue);
	public int getTotalMemory();
	public int getFreeMemory();
	public int getUsedMemory();
	public Vector<Integer> getObjectsInSequence() throws DatabaseException;
	
}
